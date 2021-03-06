import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;


public class Codelookup {

	public static final String INDEX_DIRECTORY = "indexDirectory";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";
	public static List<String> codeList = new ArrayList<String>();
	public static List<String> textList = new ArrayList<String>();
	public static List<String> searchStringList = new ArrayList<String>();
	public static Directory directory;

	public static void main(String[] args) throws Exception {
		directory = FSDirectory.open(new File("index-directory"));
		loadValues();
		createIndex();
		loadSearchStrings();
		Search();
		System.out.println("completed");
	}

	private static void Search() throws CorruptIndexException, IOException, ParseException {
		// TODO Auto-generated method stub
		Map<String,String> results = new HashMap<String, String>();
		IndexReader indexReader = IndexReader.open(directory, true);
		Searcher searcher = new IndexSearcher(indexReader);
		QueryParser parser = new QueryParser("text",new StandardAnalyzer(Version.LUCENE_CURRENT));
		Iterator<String> it3 = searchStringList.iterator();
		
		while(it3.hasNext()){
			String search = it3.next();
			//System.out.println(search);
			Query query = parser.parse(search);
			TopDocs topdocs = searcher.search(query,indexReader.maxDoc());
			//System.out.println("Number of hits: "+topdocs.totalHits);		
			ScoreDoc[] hits = topdocs.scoreDocs; 
			   	for(ScoreDoc hit: hits){
                Document documentFromSearcher = searcher.doc(hit.doc);
                System.out.println(documentFromSearcher.get("code")+" "+ documentFromSearcher.get("text")+" "+hit.score);
                results.put(documentFromSearcher.get("code"), documentFromSearcher.get("text"));
                break;
			   	}
		}
		for(Entry<String, String> entry:results.entrySet()){
	   		System.out.println(entry.getKey()+" "+ entry.getValue());			   		
	   	}
            searcher.close();
            directory.close();
		}
	

	private static void loadSearchStrings() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new FileReader("/home/ravi/Desktop/file.txt"));
		String line = null;
		while((line=reader.readLine())!=null){
			searchStringList.add(line);
		}
		reader.close();
		System.out.println(searchStringList.size());
	}

	@SuppressWarnings("deprecation")
	private static void loadValues() {
		// TODO Auto-generated method stub
		String line;
		int len;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream("/home/ravi/Desktop/indexing.csv"));
				while((line = dis.readLine()) != null){
					//System.out.println(line+"\n");
					len = line.length();
					codeList.add((String) line.subSequence(0, 10));
					//System.out.println((String) line.subSequence(0, 10));
					textList.add((String) line.subSequence(11, len));
					//System.out.println((String) line.subSequence(11, len));
				}
      		System.out.println(codeList.size());
      		System.out.println(textList.size());
      		dis.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
		
		
		IndexWriter indexWriter = new IndexWriter(directory, analyzer, true,IndexWriter.MaxFieldLength.LIMITED);
		
		java.util.Iterator<String> it1 = codeList.iterator();
		java.util.Iterator<String> it2 = textList.iterator();
		
		while(it1.hasNext()&&it2.hasNext()){
			//doc.add(new Field("id", hotel.getId(), Field.Store.YES, Field.Index.NO));
	        //doc.add(new Field("name", hotel.getName(), Field.Store.YES, Field.Index.TOKENIZED))	
			String code = it1.next();
			String text = it2.next();
			System.out.println(code);
			System.out.println(text);
			Document doc = new Document();
			doc.add(new Field("code", code, Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("text", text, Field.Store.YES, Field.Index.ANALYZED));
			indexWriter.addDocument(doc);
		}
		//indexWriter.optimize();
		indexWriter.close();
	}
}
