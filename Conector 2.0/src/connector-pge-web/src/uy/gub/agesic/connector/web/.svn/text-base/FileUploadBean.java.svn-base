package uy.gub.agesic.connector.web;

import ideasoft.util.io.IOUtilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import uy.gub.agesic.connector.util.ConnectorFileManager;

public class FileUploadBean {
	
	private UploadedFile myUploadedFile;
	private static Log log = LogFactory.getLog(FileUploadBean.class);
	
	  public UploadedFile getMyUploadedFile() {
	    return myUploadedFile;
	  }

	  public void setMyUploadedFile(UploadedFile myUploadedFile) {
	    this.myUploadedFile = myUploadedFile;
	    
	    System.out.println("=================================");
	    System.out.println(myUploadedFile.getName());
	    try {
			System.out.println("Bytes: " + myUploadedFile.getBytes());
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	  }

    private ArrayList<FileLocal> files = new ArrayList<FileLocal>();
    private int uploadsAvailable = 1;
    private boolean autoUpload = false;
    private boolean useFlash = false;
    public int getSize() {
        if (getFiles().size()>0){
            return getFiles().size();
        }else 
        {
            return 0;
        }
    }

    public FileUploadBean() {
    	
    }

    public void paint(OutputStream stream, Object object) throws IOException {
        stream.write(getFiles().get((Integer)object).getData());
    }
    public void listener(UploadEvent event) throws Exception{
    	System.out.println("listenerlistenerlistenerlistener");
		UploadItem item = event.getUploadItem();
    	System.out.println(item);
    	if (item.isTempFile()){
    		System.out.println(" ---isTempFile--");
    		
    		if (item.getFile() != null){
    			System.out.println("item.getFile()= "+item.getFile());
    			FileLocal file = new FileLocal();
    			
    			FileInputStream fis = new FileInputStream(item.getFile());
    			
    			byte[] data = IOUtilities.getByteArray(fis);
    			
    	        file.setLength(data.length);
    	        file.setName(item.getFileName());
    	        file.setData(data);
    	        files.add(file);
    	        uploadsAvailable--;
    		}
    		else{
    			System.out.println("item.getFile() NULLLLLLLLLLL");
    		}
    		
    	}
    	if (item.getData() == null){
    		System.out.println("FILE NULLLLLLLLLLL1");
    		System.out.println("FILE NULLLLLLLLLLL2");
    		System.out.println("FILE NULLLLLLLLLLL3");
    		System.out.println("FILE NULLLLLLLLLLL4");
    	}
    	else{
	        FileLocal file = new FileLocal();
	        file.setLength(item.getData().length);
	        file.setName(item.getFileName());
	        file.setData(item.getData());
	        files.add(file);
	        uploadsAvailable--;
    	}
    }  
      
    public String clearUploadData() {
        files.clear();
        setUploadsAvailable(5);
        return null;
    }
    
    public long getTimeStamp(){
        return System.currentTimeMillis();
    }
    
    public ArrayList<FileLocal> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<FileLocal> files) { 
        this.files = files;
    }

    public int getUploadsAvailable() {
        return uploadsAvailable;
    }

    public void setUploadsAvailable(int uploadsAvailable) {
        this.uploadsAvailable = uploadsAvailable;
    }

    public boolean isAutoUpload() {
        return autoUpload;
    }

    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    public boolean isUseFlash() {
        return useFlash;
    }

    public void setUseFlash(boolean useFlash) {
        this.useFlash = useFlash;
    }

}