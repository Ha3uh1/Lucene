package org.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.example.Index.GoodAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Search { 
    public static String indexDir = "./src/main/index";
    public static int N = 5;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 交互式Lucene搜索引擎 ===");
        System.out.println("1. 查询命令：#Search 后面接参数");
        System.out.println("2. 退出程序：输入 exit");
        System.out.println("===========================\n");
        while (true) {
            System.out.print("请输入命令（#Search/exit）：");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) { 
                System.out.println("程序退出中...");
                break; 
            } else if (input.startsWith("#Search")) {
                String paramsStr = input.substring("#Search".length()).trim();
                if (paramsStr.isEmpty()) { 
                    System.out.println("错误：查询命令不能为空！示例：#Search --hits=5 hurricane\n");
                    continue;
                }
                try {
                    runSearch(paramsStr);
                } catch (Exception e) {
                    System.out.println("查询失败：" + e.getMessage() + "\n");
                }
            } else { 
                System.out.println("错误：无效命令！请以#Search开头查询，或输入exit退出\n");
            }
        }
        scanner.close();
        System.out.println("程序已退出！");
    }


    private static void runSearch(String args) throws IOException, ParseException {
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        System.out.print("Q=");
        args=commandAnalysis(args);
        GoodAnalyzer analyzer = new GoodAnalyzer();
        QueryParser parser = new QueryParser("TEXT", analyzer);
        Query query = parser.parse(args);
        long startTime = System.currentTimeMillis();
        TopDocs topDocs = searcher.search(query, N);
        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        System.out.print("(took " + elapsedTime + " seconds" + ")\n");
        System.out.print("\n");
        if (scoreDocs.length == 0) { 
            System.out.println("查询结果为空！\n");
            reader.close();
            dir.close();
            return;
        }
        for (int j = 0; j < scoreDocs.length; j++) {
            ScoreDoc scoreDoc = scoreDocs[j];
            org.apache.lucene.document.Document doc = searcher.doc(scoreDoc.doc);
            String docID = doc.get("DOCNO");
            float score = scoreDoc.score;
            String text = doc.get("TEXT");
            String summary = text.length() <= 400 ? text : text.substring(0, 400) + "……";
            System.out.printf("%02d[%.4f] %s %s%n",
                    (j + 1),
                    score,
                    docID,
                    summary
            );
            System.out.println("\n");
        }
        reader.close();
        dir.close();
    }



    private static String commandAnalysis(String arg) {
        if (arg.startsWith("--hits=")) {
            int end = arg.indexOf(" ");
            String hits = arg.substring(7,end);
            try {
                N = Integer.parseInt(hits);
                if (N <= 0) { 
                    System.out.println("警告：--hits需为正数，使用默认值" + 5);
                    N = 5;
                }
            } catch (NumberFormatException e) {
                System.out.println("无效--hits参数：" + arg + "，使用默认值" + 5);
                N = 5;
            }
            return arg.substring(end+1);
        }
        return arg;
    }
}
