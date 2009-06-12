package com.browseengine.bobo.demo.cardemo.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

import org.hibernate.classic.Session;

import com.browseengine.bobo.demo.cardemo.DemoCar;

public class DemoCarDBLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		File datafile = new File(args[0]);
		
		DemoCarDBLoader dbLoader = new DemoCarDBLoader();
		
		dbLoader.loadCars(datafile);
		
		SessionFactoryUtil.getInstance().close();
	}
	
	private static DemoCar makeCar(int count,Properties props){
		DemoCar car = new DemoCar();
        car.setId(count);
        car.setColor(props.getProperty("color"));
        car.setCategory(props.getProperty("category"));
        car.setCity(props.getProperty("city"));
        car.setMakeModel(props.getProperty("makemodel"));
        car.setYear(Integer.parseInt(props.getProperty("year")));
        car.setPrice(Float.parseFloat(props.getProperty("price")));
        car.setMileage(Integer.parseInt(props.getProperty("mileage")));
        
        String tagString = props.getProperty("tags");
        String[] tags = tagString.split(",");
        car.setTags(Arrays.asList(tags));
        return car;
	}
	
	private void loadCars(File dataFile) throws IOException{
		FileInputStream fin=null;
		int count = 0;
        Session session = null;
        try
        {
        	session = SessionFactoryUtil.getInstance().openSession();
        	session.beginTransaction();
			try{
				fin=new FileInputStream(dataFile);
				BufferedReader reader=new BufferedReader(new InputStreamReader(fin,"UTF-8"));
				String line=null;
				Properties prop=new Properties();
				while(true){
					line=reader.readLine();
					if (line==null){
						break;
					}
					if ("<EOD>".equals(line)){		//new record
						DemoCar car=makeCar(count++,prop);
						session.save(car);
					}
					else{
						int index=line.indexOf(":");
						if (index!=-1){
							String name=line.substring(0, index);
							String val=line.substring(index+1,line.length());
							prop.put(name.trim(), val.trim());
						}
					}
				}
				reader.close();
			}
			finally{
				if (fin!=null){
					fin.close();
				}
			}
			session.getTransaction().commit();
        }
        finally
        {
        	if (session!=null){
        		session.close();
        	}
        }
	}

	private void createAndStoreCar(File dataFile) {
		
        Session session = SessionFactoryUtil.getInstance().openSession();

        session.beginTransaction();

        DemoCar car = new DemoCar();
        car.setId(1);
        car.setColor("red");

        session.save(car);

        session.getTransaction().commit();
    }
}
