package File;

import java.io.IOException;
import java.util.TimerTask;

import org.json.JSONException;

import utils.Packer;

public class GetPackageByTimer extends TimerTask{
	
	private String path;
	
	public GetPackageByTimer(String path){
		this.path = path;
	}
	 @Override

	  public void run() {

	    System.out.println("TestTimerTask is running......");
	    Packer packer = new Packer(path, path+"//test.zip");
        try {
			packer.packupSuffix(".txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    

	  }
}
