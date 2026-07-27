package edu.repetita.solvers.sr.rls.structure;

public class SortUtils {

    public static void mergeSort(int[] elements, int[] keys) {
        mergeSort(elements, keys, 0, elements.length);
    }

    public static void mergeSort(int[] elements, int[] keys, int base, int topExcluded) {
        int n = elements.length;
        int[] runs = new int[n + 1];
        int[] aux = new int[n];
        mergeSort(elements, keys, base, topExcluded, runs, aux);
    }

    public static void mergeSort(int[] elements, int[] keys, int base, int topExcluded, int[] runs, int[] aux) {
        int n = elements.length;
        assert (base >= 0);
        assert (topExcluded <= n);

        if (topExcluded - base > 1) {
            int el = base;
            int rSize = 1;
            int rP = 0;

            boolean keepGoing = true;
            while (keepGoing) {
                el++;
                while (el < topExcluded && keys[elements[el - 1]] <= keys[elements[el]]) {
                    rSize++;
                    el++;
                }
                runs[rP] = rSize;
                rSize = 1;
                rP++;
                keepGoing = el < topExcluded;
            }
            runs[rP] = 0;

            if (rP > 1) {
                int finalBase = base;
                int finalTop = topExcluded;

                if (rP <= 4) {
                    int minLocation = base + runs[0];
                    int minUnsorted = keys[elements[minLocation]];

                    int runP = 1;
                    while (runP < rP - 1) {
                        minLocation += runs[runP];
                        minUnsorted = Math.min(minUnsorted, keys[elements[minLocation]]);
                        runP++;
                    }

                    int maxLocation = minLocation - 1;
                    int maxUnsorted = keys[elements[maxLocation]];

                    runP--;

                    while (runP > 0) {
                        maxLocation -= runs[runP];
                        maxUnsorted = Math.max(maxUnsorted, keys[elements[maxLocation]]);
                        runP--;
                    }

                    while (keys[elements[finalBase]] < minUnsorted) finalBase++;
                    while (keys[elements[finalTop - 1]] > maxUnsorted) finalTop--;
                    runs[0] -= finalBase - base;
                    runs[rP - 1] -= topExcluded - finalTop;
                }

                int whichArray = mergeSort1(elements, aux, keys, runs, rP + 1, finalBase, 0);
                if (whichArray == 1) {
                    System.arraycopy(aux, finalBase, elements, finalBase, finalTop - finalBase);
                }
            }
        }
    }

    private static int mergeSort1(int[] tab1, int[] tab2, int[] keys, int[] runs, int runsSize, int base, int which) {
        int runP = 0;
        int baseP = base;

        while (runP + 1 < runsSize) {
            int list1P = baseP;
            int limit1 = baseP + runs[runP];
            int list2P = limit1;
            int limit2 = limit1 + runs[runP + 1];
            int tab2P = baseP;

            while (list1P < limit1 && list2P < limit2) {
                int e1 = tab1[list1P];
                int e2 = tab1[list2P];
                if (keys[e1] <= keys[e2]) {
                    tab2[tab2P] = e1;
                    list1P++;
                } else {
                    tab2[tab2P] = e2;
                    list2P++;
                }
                tab2P++;
            }

            if (list1P < limit1) {
                System.arraycopy(tab1, list1P, tab2, tab2P, limit1 - list1P);
            } else {
                System.arraycopy(tab1, list2P, tab2, tab2P, limit2 - list2P);
            }

            baseP = limit2;
            runs[runP >> 1] = runs[runP] + runs[runP + 1];
            runP += 2;
        }

        if (runP < runsSize) {
            runs[runP >> 1] = runs[runP];
            System.arraycopy(tab1, baseP, tab2, baseP, runs[runP]);
        }

        int newRunsSize = (runsSize + 1) >> 1;
        if (newRunsSize == 1) {
            return 1 - which;
        } else {
            return mergeSort1(tab2, tab1, keys, runs, newRunsSize, base, 1 - which);
        }
    }
}
