import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

class Artist{
    final String name, genre, stage;
    final int day, duration, popularity;
    Artist(String n,String g,int d,String s,int du,int p){name=n;genre=g;day=d;stage=s;duration=du;popularity=p;}
    static Artist fromCsv(String line){
        String[] p=line.split(";");
        return new Artist(p[0].trim(),p[1].trim(),Integer.parseInt(p[2].trim()),p[3].trim(),Integer.parseInt(p[4].trim()),Integer.parseInt(p[5].trim()));
    }
    String getName(){return name;}
    String getStage(){return stage;}
    int getDay(){return day;}
    int getDuration(){return duration;}
    int getPopularity(){return popularity;}
    public String toString(){return String.format("%s (%s) - %d min - pop %d",name,genre,duration,popularity);}
}

public class EDASession5lab{
    static final DateTimeFormatter TF=DateTimeFormatter.ofPattern("HH:mm");
    public static void main(String[] args) throws Exception{
        File csv = findCsv();
        if(args!=null && args.length>0){ File f=new File(args[0]); if(f.exists() && f.isFile()) csv=f; }
        if(csv==null){ System.out.println("artists.csv not found."); return; }
        List<Artist> all = read(csv);
        System.out.println("Loaded: "+csv.getAbsolutePath()+"\n");
        for(int day=1;day<=3;day++){ System.out.println("Day "+day+"\n----------"); processDay(day,filter(all,day)); System.out.println(); }
    }

    static File findCsv() {
        String target = "artists.csv";
        
        // Busca en el directorio de trabajo actual
        String cwd = System.getProperty("user.dir");
        File f1 = new File(cwd, target);
        if (f1.exists() && f1.isFile()) return f1;
        
        // Busca en el directorio actual relativo
        File f2 = new File(".", target);
        if (f2.exists() && f2.isFile()) return f2;
    
        // Busca en el home del usuario
        String home = System.getProperty("user.home");
        if (home != null) {
            File f3 = new File(home, target);
            if (f3.exists() && f3.isFile()) return f3;
        }
    
        // Busca recursivamente en subcarpetas
        List<String> commonDirs = Arrays.asList("src", "data", "resources");
        for (String d : commonDirs) {
            File candidate = new File(cwd, d + File.separator + target);
            if (candidate.exists() && candidate.isFile()) return candidate;
        }
    
        System.out.println("No se encontró '" + target + "'. Se buscó en:");
        System.out.println(" - " + cwd);
        System.out.println(" - " + f2.getAbsolutePath());
        if (home != null) System.out.println(" - " + home);
        System.out.println(" - subcarpetas: src/, data/, resources/");
        return null;
    }
    

    static File search(File base,String name){
        if(base==null||!base.exists()) return null;
        Queue<File> q=new ArrayDeque<>(); q.add(base);
        while(!q.isEmpty()){
            File cur=q.poll();
            File[] ch=cur.listFiles();
            if(ch==null) continue;
            for(File c:ch){
                if(c.isFile() && c.getName().equalsIgnoreCase(name)) return c;
                if(c.isDirectory()) q.add(c);
            }
        }
        return null;
    }

    static List<Artist> read(File f) throws IOException{
        List<Artist> r=new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new FileReader(f))){
            String line;
            while((line=br.readLine())!=null){
                String t=line.trim(); if(t.isEmpty()) continue;
                String[] p=t.split(";");
                if(p.length<6) continue;
                try{ Integer.parseInt(p[2].trim()); }catch(Exception ex){ continue; }
                try{ r.add(Artist.fromCsv(t)); }catch(Exception ex){}
            }
        }
        return r;
    }

    static List<Artist> filter(List<Artist> all,int day){
        List<Artist> r=new ArrayList<>(); for(Artist a:all) if(a.getDay()==day) r.add(a); return r;
    }

    static void processDay(int day,List<Artist> dayList){
        List<Artist> main=new ArrayList<>(), river=new ArrayList<>();
        for(Artist a:dayList) if(a.getStage().equalsIgnoreCase("Main")) main.add(a); else river.add(a);
        Comparator<Artist> cmp=Comparator.comparingInt(Artist::getPopularity).thenComparingInt(Artist::getDuration).thenComparing(a->a.getName().toLowerCase());
        Collections.sort(main,cmp); Collections.sort(river,cmp);
        System.out.println("Main Stage schedule:"); StageStats s1=printSchedule(LocalTime.of(14,0),main);
        System.out.println("\nRiver Stage schedule:"); StageStats s2=printSchedule(LocalTime.of(13,30),river);
        System.out.println("\nMain Stage summary:"); printSummary(s1);
        System.out.println("\nRiver Stage summary:"); printSummary(s2);
    }

    static StageStats printSchedule(LocalTime start,List<Artist> list){
        int total=0,sumPop=0; LocalTime t=start;
        for(Artist a:list){
            System.out.printf("%s - %s%n",t.format(TF),a);
            total+=a.getDuration(); sumPop+=a.getPopularity(); t=t.plusMinutes(a.getDuration()+30);
        }
        String head=list.isEmpty()?"None":list.get(list.size()-1).getName();
        double avg=list.isEmpty()?0.0:(double)sumPop/list.size();
        return new StageStats(total,avg,head);
    }

    static void printSummary(StageStats s){
        System.out.println("Total music time: "+s.total+" minutes");
        System.out.printf("Average popularity: %.2f%n",s.avg);
        System.out.println("Headliner: "+s.headliner);
    }

    static class StageStats{ final int total; final double avg; final String headliner; StageStats(int t,double a,String h){total=t;avg=a;headliner=h;} }
}