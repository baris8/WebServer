package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class Test {
    
    public Mimes mimes;
    
    public Test() throws IOException{
        mimes = new Mimes("C:\\Users\\Baris\\Documents\\NetBeansProjects\\WebServer\\src\\test\\mime.types");
        mimes.makeTable();
    }
    
    
    /*public static void main(String argv[]) throws Exception{
        Test t = new Test();
        String s = t.mimes.getValueOfKey(".html");
        System.out.print(s);
   }*/
        
}

class Mimes{
        Hashtable<String, String> table;
        File f;
        
        public Mimes(String PATH){
            table = new Hashtable<>();
            f = new File(PATH);
        }
        
        
        public void makeTable() throws FileNotFoundException, IOException{
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {

                StringTokenizer st = new StringTokenizer(line);
                String type;
                if(st.hasMoreTokens()){
                    type = st.nextToken();
                    if(!type.startsWith("#") && !type.startsWith("\n")){
                        while (st.hasMoreTokens()) { 
                            String filetype = st.nextToken();
                            table.put(filetype, type);
                        }
                    }
                }
            }
        }
        
        public String getValueOfKey(String key){
            if(table.get(key) != null){
                return table.get(key); 
            }
            return "application/...";
        }
}
