import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
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
    size = end - start + 1;
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

  public int getNumExons()
  {
    return exonList.size();
  }

  public int getNumIntrons()
  {
    return exonList.size() - 1;
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

  public int getIntronSize()
  {
    return getCDSSpan() + 3 - getCDSSize();
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
  private long CDSSpanSum;
  private long CDSSizeSum;
  private int numExons;
  private long intronSizeSum;
  private int numIntrons;
  private int maxCDSSize;
  private long totalCDS;
  
  public Gene()
  {
    isoList = new HashMap<String, Isoform>();
  }

  public int getMaxCDSSize()
  {
    return maxCDSSize;
  }

  public long getTotalCDS()
  {
    return totalCDS;
  }

  public long getIntronSizeSum()
  {
    return intronSizeSum;
  }

  public int getNumIntrons()
  {
    return numIntrons;
  }

  public int getNumExons()
  {
    return numExons;
  }

  public long getCDSSizeSum()
  {
    return CDSSizeSum;
  }

  public long getCDSSpanSum()
  {
    return CDSSpanSum;
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

  public int getIsoSize()
  {
    return isoList.size();
  }

  public void calculate()
  {
    numExons = 0;
    CDSSpanSum = 0;
    CDSSizeSum = 0;
    intronSizeSum = 0;
    Iterator<Entry<String, Isoform>> it = isoList.entrySet().iterator();
    ArrayList<Integer> maxExons = null;
    totalCDS = 0L;
    while (it.hasNext())
    {
      Isoform isoform = it.next().getValue();
      CDSSpanSum += isoform.getCDSSpan();
      CDSSizeSum += isoform.getCDSSize();
      numExons += isoform.getNumExons();
      intronSizeSum += isoform.getIntronSize();
      numIntrons += isoform.getNumIntrons();
      if(maxExons.size() == 0)
      {
        maxExons = new ArrayList<Integer>();
        for(int i = 0; i < isoform.getNumExons(); i++)
        {
          maxExons.add(new Integer(isoform.getExon(i).size));
        }
      }
      else
      {
        for(int i = 0; i < isoform.getNumExons(); i++)
        {
          if(maxExons.get(i) < isoform.getExon(i).size)
            maxExons.set(i, new Integer(isoform.getExon(i).size));
        }
      }
      // it.remove(); // avoids a ConcurrentModificationException
    }
    for(int i = 0; i < maxExons.size(); i++)
    {
      totalCDS += maxExons.get(i);
    }
  }

}

class DNASequence
{
  HashMap<String, Gene> geneList;
  private double averageCDSSpan;
  private double averageCDSSize;
  private double averageExonSize;
  // cdssize - exonsize?
  private double averageIntronSize;
  private double averageIntergenicRegion;
  private double averageCDSSpanPerTotal;
  private double averageCDSSizePerTotal;
  private double averageGenesPer10kb;
  private double totalNuc;
  private double totalCDSPerTotal;
  
  public DNASequence()
  {
    geneList = new HashMap<String, Gene>();
  }

  public double getAverageIntronSize()
  {
    return averageIntronSize;
  }

  public double getAverageIntergenicRegion()
  {
    return averageIntergenicRegion;
  }

  public double getAverageCDSSpanPerTotal()
  {
    return averageCDSSpanPerTotal;
  }

  public double getAverageCDSSizePerTotal()
  {
    return averageCDSSizePerTotal;
  }

  public double getAverageGenesPer10kb()
  {
    return averageGenesPer10kb;
  }

  public double getTotalNuc()
  {
    return totalNuc;
  }

  public double getAverageExonSize()
  {
    return averageExonSize;
  }

  public double getAverageCDSSpan()
  {
    return averageCDSSpan;
  }

  public double getAverageCDSSize()
  {
    return averageCDSSize;
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

  public void setNuc(int totalNuc)
  {
    this.totalNuc = totalNuc;
  }

  public void calculate()
  {
    long sumCDSSpan = 0L;
    long sumCDSSize = 0L;
    int sumNumExons = 0;
    int sumNumIso = 0;
    long sumIntronSize = 0;
    int sumNumIntrons = 0;
    long totalCDS = 0L;
    Iterator<Entry<String, Gene>> it = geneList.entrySet().iterator();
    while (it.hasNext())
    {
      Gene gene = (Gene) it.next().getValue();
      gene.calculate();
      sumNumIso += gene.getIsoSize();
      sumCDSSpan += gene.getCDSSpanSum();
      sumCDSSize += gene.getCDSSizeSum();
      sumNumExons += gene.getNumExons();
      sumIntronSize += gene.getIntronSizeSum();
      sumNumIntrons += gene.getNumIntrons();
      
      totalCDS += gene.getTotalCDS();
      // it.remove(); // avoids a ConcurrentModificationException
    }
    averageCDSSpan = (double) sumCDSSpan / sumNumIso;
    averageCDSSize = (double) sumCDSSize / sumNumIso;
    averageExonSize = (double) sumCDSSize / sumNumExons;
    averageIntronSize = (double) sumIntronSize / sumNumIntrons;

    averageIntergenicRegion = 0;
    averageCDSSpanPerTotal = (double) sumCDSSpan / totalNuc;
    averageCDSSizePerTotal = (double) sumCDSSize / totalNuc;
    averageGenesPer10kb = geneList.size() /totalNuc / 10000;
    totalCDSPerTotal = (double) totalCDS / totalNuc;
  }
}

public class driver
{
  public static void main(String[] args)
  {
    DNASequence sequence = new DNASequence();
    parseGFFFile("test.gff", sequence);
    String nucleotides = readFastaFile("test.fasta");
    sequence.setNuc(nucleotides.length());

    sequence.calculate();

    System.out.println(sequence.getAverageCDSSpan());
    System.out.println(sequence.getAverageCDSSize());
    System.out.println(sequence.getAverageExonSize());
    System.out.println(sequence.getAverageIntronSize());
    System.out.println(sequence.getAverageIntergenicRegion());
    System.out.println(sequence.getAverageCDSSpanPerTotal());
    System.out.println(sequence.getAverageCDSSizePerTotal());
    System.out.println(sequence.getAverageGenesPer10kb());
    System.out.println(sequence.getTotalNuc());

    System.out.println(sequence.geneList);

  }

  public static void parseGFFFile(String path, DNASequence sequence)
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
        if (splitLine[2].equals("mRNA") || splitLine[2].equals("CDS"))
        {
          // 9 genename
          // 11 isoname
          // 2 cds/mrna
          // 3 start
          // 4 end
          // 6 +/-
          // System.out.println(currLine);
          // System.out.println(splitLine[0]);
          if (splitLine.length > 10)
            sequence.add(new GFFEntry(splitLine[9], splitLine[11],
                splitLine[2], splitLine[3], splitLine[4], splitLine[6]));
        }

      }

      fScanner.close();
    } catch (FileNotFoundException e)
    {
      System.out.println("File " + path + " not found.");
    }
  }

  public static String readFastaFile(String path)
  {
    Scanner sc;
    try
    {
      sc = new Scanner(new File(path));
    } catch (Exception e)
    {
      sc = new Scanner("");
    }

    String sequence;
    StringBuilder sb = new StringBuilder();
    if (sc.hasNextLine())
    {
      sc.nextLine();
    }

    while (sc.hasNextLine())
    {
      sb.append(sc.nextLine());
    }

    sequence = sb.toString();
    sequence = sequence.replace("\n", "");
    sequence = sequence.replaceAll("\\s+", "");
    sc.close();
    return sequence;
  }
}
