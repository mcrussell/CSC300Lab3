public class Region {
   boolean forward;
   int start;
   int end;

   public Region(int start, int end, boolean forward) {
      this.start = start;
      this.end = end;
      this.forward = forward;
   }
}
