package main;

import java.util.Timer;


import File.GetPackageByTimer;
import File.SaveToFile;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Timer timer;
		SaveToFile saveToFile = new SaveToFile("D:\\");
		
		timer = new Timer();
		timer.schedule(new GetPackageByTimer(saveToFile.getDirectoryPath()), 0,3000);
		
		for(int i=0;i<300;i++)
		{
			saveToFile.write("write "+i);
			try {
				Thread.sleep(1000);
				System.out.println(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
