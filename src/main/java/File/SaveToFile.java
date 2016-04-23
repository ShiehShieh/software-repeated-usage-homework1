package File;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import CM.GetConfiguration;


public class SaveToFile {
	private String path;
	BufferedWriter writer;
	GetConfiguration g;
	public SaveToFile(){
		g = new GetConfiguration();
		path = g.getPATH();
		try {
			getBufferedWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SaveToFile(String s){
		path = s;
		try {
			getBufferedWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getBufferedWriter() throws IOException{
		long timestamp = System.currentTimeMillis();
		String directoryPath = path+"\\"+timestamp;
		File directory = new File(directoryPath);
		if(!directory.exists()&&!directory.isDirectory()){
			directory.mkdir();
			//System.out.println("directory");
		}
		
		path=directoryPath+"\\history.txt";
		File file = new File(path);
		if(!file.exists()){
			boolean res = file.createNewFile();
			if(!res)
				System.out.println("创建文件失败！");
		}
		writer = new BufferedWriter(new FileWriter(path));
	}
	
	public void write(String str){
		try {
			writer.write(str+"\r\n");
			writer.flush();
			//System.out.println(str+"\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void finalized(){
		if(writer != null){
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		SaveToFile s = new SaveToFile();
		s.write("first");
		s.write("second");
	}
}
