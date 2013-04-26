import java.util.HashMap;
import java.util.Scanner;
import java.util.Collections;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.io.FileNotFoundException;

class GFFEntry
{
  String geneName;
  String isoName;
  String type;
  Integer start;
  Integer end;
  Boolean positive;

  public GFFEntry(String geneName, String isoName, String type, String start,
      String end, String positive)
  {
    this.geneName = geneName;
    this.isoName = isoName;
    this.type = type;
    this.start = Integer.valueOf(start);
    this.end = Integer.valueOf(end);
    this.positive = (positive == "+");
  }
}

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
    if (other.start == start)
    {
      System.out.println("EXON START VALUES EQUAL, NEED TEST CASE");
    }
    return other.start - start;
  }
}

class Isoform
{
  private ArrayList<DNARegion> exonList = new ArrayList<DNARegion>();
  private String name;
  private int mRNAstart;
  private int mRNAend;

  public Isoform(String name, int mRNAstart, int mRNAend)
  {
    this.name = name;
    this.mRNAstart = mRNAstart;
    this.mRNAend = mRNAend;
  }

  public Isoform()
  {

  }

  public void add(GFFEntry entry)
  {
    if (entry.type.equals("CDS"))
    {
      addExon(entry.start, entry.end);
    }
    else
    {
      mRNAstart = entry.start;
      mRNAend = entry.end;
    }
  }

  // Assume + direction
  public void addExon(int CDSstart, int CDSend)
  {
    exonList.add(new DNARegion(CDSstart, CDSend));
  }

  public DNARegion getExon(int index)
  {
    return exonList.get(index);
  }

  public int numExons()
  {
    return exonList.size();
  }

  // Includes end codon assumes positive
  public int getCDSSpan()
  {
    return mRNAend - mRNAstart + 3;
  }

  public int getCDSSize()
  {
    int span = 0;
    for (int i = 0; i < exonList.size(); i++)
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
    for (int i = 0; i < exonList.size() - 1; i++)
    {
      intronList.add(new DNARegion(exonList.get(i + 1).end,
          exonList.get(i).start));
    }
    return intronList;
  }
}

class Gene
{
  HashMap<String, Isoform> isoList;

  public Gene()
  {
    isoList = new HashMap<String, Isoform>();
  }

  public void calculateMaxCDS()
  {
    // TODO
  }

  public void add(GFFEntry entry)
  {
    if (isoList.containsKey(entry.isoName))
    {
      isoList.get(entry.isoName).add(entry);
    }
    else
    {
      Isoform isoform = new Isoform();
      isoform.add(entry);
      isoList.put(entry.isoName, isoform);
    }
  }
}

class DNASequence
{
  HashMap<String, Gene> geneList;

  public DNASequence()
  {
    geneList = new HashMap<String, Gene>();
  }

  public void add(GFFEntry entry)
  {
    if (geneList.containsKey(entry.geneName))
    {
      geneList.get(entry.geneName).add(entry);
    }
    else
    {
      Gene gene = new Gene();
      gene.add(entry);
      geneList.put(entry.geneName, gene);
    }
  }
}

public class driver
{
  public static void main(String[] args)
  {
    DNASequence gene = new DNASequence();
    parseFile("test.gff", gene);
  }

  public static void parseFile(String path, DNASequence gene)
  {
    try
    {
      FileInputStream fstream = new FileInputStream(path);
      Scanner fScanner = new Scanner(fstream);
      String currLine = new String();
      while (fScanner.hasNextLine())
      {
        currLine = fScanner.nextLine();
        String[] splitLine = currLine.split("\\s+");
        // if (splitLine[2].equals("mRNA") || splitLine[2].equals("CDS"))
        // {
        // 9 genename
        // 11 isoname
        // 2 cds/mrna
        // 3 start
        // 4 end
        // 6 +/-
        // System.out.println(currLine);
        // System.out.println(splitLine[0]);
        if (splitLine.length > 10)
          gene.add(new GFFEntry(splitLine[9], splitLine[11], splitLine[2],
              splitLine[3], splitLine[4], splitLine[6]));
        // }

      }

      //gene.getAverageCDSSpan();
      System.out.println(gene.geneList);
    } catch (FileNotFoundException e)
    {
      System.out.println("File " + path + " not found.");
    }
  }
}
