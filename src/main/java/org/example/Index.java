package org.example;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import  java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import  java.util.regex.*;

public class Index {
    public static String[] field={"DOCNO","TEXT"};
    public static void main(String[] args) throws IOException {
        File dir = new File("./src/main/resources/tdt3");
        File outdir=new File("./src/main/index");
        outdir.mkdirs();
        try {
            Directory directory = FSDirectory.open(Paths.get(outdir.getPath()));
            GoodAnalyzer analyzer = new GoodAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(directory, config);
            File[] files = dir.listFiles();
            for (File dire : files) {
                if (dire.isDirectory()) {
                    for (File f : dire.listFiles()) {
                        indexCreator(f,writer);
                    }
                }
                else {
                    indexCreator(dire,writer);
                }
            }
            writer.commit();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static String search(String index,String content){
        Pattern pattern = Pattern.compile("<"+index+">"+"\\s*(.*?)\\s*"+"</"+index+">",Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
    private static void indexCreator(File file,IndexWriter indexWriter) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String content = new String(bytes, StandardCharsets.UTF_8);
        Document doc = new Document();
        doc.add(new StringField(Index.field[0],search(Index.field[0],content), Field.Store.YES));
        doc.add(new TextField(Index.field[1],search(Index.field[1],content), Field.Store.YES));
        indexWriter.addDocument(doc);
        System.out.println(file.getName()+"is successfully indexed ");
    }
    public static class GoodAnalyzer extends Analyzer{
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            StandardTokenizer tokenizer = new StandardTokenizer();
            TokenStream stream = new LowerCaseFilter(tokenizer);
            stream = new StopFilter(stream, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
            stream = new PorterStemFilter(stream);
            return new TokenStreamComponents(tokenizer, stream);
        }
    }
}
