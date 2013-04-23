import java.util.Scanner;
import java.util.Collections;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.io.FileNotFoundException;

class DNARegion implements Comparable
{
  public int start;
  public int end;
  public int size;

  public DNARegion(int start, int end)
  {
    this.start = start;
    this.end = end;
    size = end - start;
  }
  public int compareTo(Object otherObject)
  {
    DNARegion other = (DNARegion) otherObject;
    if(other.start == start)
    {
      System.out.println("EXON START VALUES EQUAL, NEED TEST CASE");
    }
    return other.start - start; 
  }
}

class Isoform
{
  private ArrayList<DNARegion> exonList;
  private String name;
  private int mRNAstart;
  private int mRNAend;

  public Isoform(String name, int mRNAstart, int mRNAend)
  {
    this.name = name;
    this.mRNAstart = mRNAstart;
    this.mRNAend = mRNAend; 
    exonList = new ArrayList<DNARegion>();
  }  
  //Assume + direction
  public void addDNARegion(int CDSstart, int CDSend)
  {
    exonList.add(new DNARegion(CDSstart, CDSend));
    Collections.sort(exonList);
  }
  public DNARegion getExon(int index)
  {
    return exonList.get(index);
  }
  public int numExons()
  {
    return exonList.size();
  }

  //Includes end codon
  public int getCDSSpan()
  {
    return mRNAend - mRNAstart + 3;
  } 
  public int getCDSSize()
  {
    int span = 0;
    for(int i = 0; i < exonList.size(); i++)
    {
      DNARegion temp = exonList.get(i);
      span += temp.size;
    }
    return span;
  }
  public ArrayList<DNARegion> getExons()
  {
    return exonList;
  }
  public ArrayList<DNARegion> generateIntronList()
  {
    ArrayList<DNARegion> intronList = new ArrayList<DNARegion>();
    for(int i = 0; i < exonList.size()-1; i++)
    {
      intronList.add(new DNARegion(exonList.get(i+1).end, exonList.get(i).start));
    }
    return intronList;
  }
}

class Gene
{
  private ArrayList<Isoform> isoforms;
  public Gene()
  {
    isoforms = new ArrayList<Isoform>();  
  }
  public void newIsoform(String name, int start, int end)
  {
    //TODO
  }
  public void addExon(String name, int start, int end)
  {
    //TODO
  }  
  public void calculateMaxCDS()
  {
    //TODO
  }
}

class GeneSequence
{
  ArrayList<Gene> geneList;
  public GeneSequence()
  {
    geneList = new ArrayList<Gene>();
  }
  public void add(String one, String two, String three, String four, String five, String six)
  {
    //TODO
  }
}

public class driver
{
  public static void main(String [] args)
  {
    GeneSequence gene = new GeneSequence();
    parseFile("test.gff", gene);
  }
  public static void parseFile(String path, GeneSequence gene)
  {
    try {
      FileInputStream fstream = new FileInputStream(path);
      Scanner fScanner = new Scanner(fstream);
      String currLine = new String();
      while(fScanner.hasNextLine()) {
        currLine += fScanner.nextLine();
        String[] splitLine = currLine.split(" ");
        if(splitLine[2].equals("mRNA") || splitLine[2].equals("CDS"))
        {
          gene.add(splitLine[9],splitLine[11],splitLine[2],splitLine[3],splitLine[4], splitLine[6]);
        }
      }

    }
    catch(FileNotFoundException e) {
      System.out.println("File " + path + " not found.");
    }
  }
}
