package utils;

import java.io.IOException;
import java.sql.SQLException;

import resources.ResourcesLoader;

public class testDBAccess {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		DatabaseAccess access;
		try {
			ResourcesLoader resources_loader = new ResourcesLoader();
			resources_loader.load();
			access = DatabaseAccess.getDatabaseAccess();
			access.test();
//			slogger_.info("column pair count: " + access.getColumnPairCount("Anarchism", "John_Wyndham"));
//			slogger_.info("row pair count: " + access.getRowPairCount("George_Balanchine", "La_Valse"));
//			slogger_.info("header column count: " + access.getHeaderCellCount("000_Bon_Jovi_Fans_Can''t_Be_Wrong", "Kilometre,50510,1,000"));
//			slogger_.info("header header count: " + access.getHeaderHeaderCount("A", "D"));
//			slogger_.info("mention count: " + access.getMentionCount("Carmine Coppola", "Carmine_Coppola"));
//			double[] values = access.getHeaderValues("Atomic_time");
//			slogger_.info("Header min value: " + values[0] +", max value: " + values[1]);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}

}
