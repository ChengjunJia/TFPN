package i2analysis;


import java.util.*;

class test {
    static class Interval implements Comparable<Interval>{
        Integer min;
        Integer max;
        public Interval(int min, int max) {
            this.min = min;
            this.max = max;
        }

        boolean intersects(int num) {
            return (min <= num && max >= num);
        }

        //Overrides the compareTo method so it will be sorted
        //in order relative to the min value
        @Override
        public int compareTo(Interval obj) {
            if (min > obj.min) return 1;
            else if (min < obj.min) return -1;
            else return 0;
        }
    }

    public static Set<Interval> smallestIntervalSet(Interval[] set, Interval target) {
        //Bottleneck is here. The array is sorted, giving this algorithm O(nlogn) time
        Arrays.sort(set);

        //Create a set to store our ranges in
        Set<Interval> smallSet = new HashSet<Interval>();
        //Create a variable to keep track of the most optimal range, relative
        //to the range before it, at all times.
        Interval bestOfCurr = null;
        //Keep track of the specific number that any given range will need to
        //intersect with. Initialize it to the target-min-value.
        int currBestNum = target.min;
        //Go through each element in our sorted array.
        for (int i = 0; i < set.length; i++) {
            Interval currInterval = set[i];
            //If we have already passed our target max, break.
            if (currBestNum >= target.max)
                break;
            //Otherwise, if the current interval intersects with
            //our currBestNum
            if (currInterval.intersects(currBestNum)) {
                //If the current interval, which intersects currBestNum
                //has a greater max, then our current bestOfCurr
                //Update bestOfCurr to be equal to currInterval.
                if (bestOfCurr == null || currInterval.max >= bestOfCurr.max) {
                    bestOfCurr = currInterval;
                }
            }
            //If our range does not intersect, we can assume that the most recently
            //updated bestOfCurr is probably the most optimal new range to add to 
            //our set. However, if bestOfCurr is null, it means it was never updated,
            //because there is a gap somewhere when trying to fill our target range.
            //So we must check for null first.
            else if (bestOfCurr != null) {
                //If it's not null, add bestOfCurr to our set
                smallSet.add(bestOfCurr);
                //Update currBestNum to look for intervals that
                //intersect with bestOfCurr.max
                currBestNum = bestOfCurr.max;
                //This line is here because without it, it actually skips over
                //the next Interval, which is problematic if your sorted array
                //has two optimal Intervals next to eachother.
                i--;
                //set bestOfCurr to null, so that it won't run
                //this section of code twice on the same Interval.
                bestOfCurr = null;
            }

        }

        //Now we should just make sure that we have in fact covered the entire
        //target range. If we haven't, then we are going to return an empty list.
        if (currBestNum < target.max)
            smallSet.clear();
        return smallSet;
    }

    public static void main(String[] args) {
        //{(1, 4), (30, 40), (20, 91) ,(8, 10), (6, 7), (3, 9), (9, 12), (11, 14)}
        Interval[] interv = {
                new Interval(1, 4),
                new Interval(30, 40),
                new Interval(20, 91),
                new Interval(8, 10),
                new Interval(6, 7),
                new Interval(3, 9),
                new Interval(10, 12),
                new Interval(11, 14)
        };
        Set<Interval> newSet = smallestIntervalSet(interv, new Interval(3,14));
        for (Interval intrv : newSet) {
            System.out.print("(" + intrv.min + ", " + intrv.max + ") ");
        }

    }
}

