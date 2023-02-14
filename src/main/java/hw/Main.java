package hw;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        List<Thread> threads = new ArrayList<>(); //список для хранения создаваемых потоков

        Thread sizeToFreqMaxValuePrinter = new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                        int maxValueKey = getSizeToFreqMaxValue();
                        if (maxValueKey > 0) {
                            System.out.println("Значение " + maxValueKey + " (встретилось " + sizeToFreq.get(maxValueKey) + " раз)");
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        sizeToFreqMaxValuePrinter.start();

        int streaming = 1000;
        for (int i = 0; i < streaming; i++) {
            Thread thread = new Thread(generateRoute("RLRFR", 100));
            //thread.setName("Name-" + i);
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                return;
            }
        }

        sizeToFreqMaxValuePrinter.interrupt();

        int maxValueKey = getSizeToFreqMaxValue();
        System.out.println("\nСамое частое количество повторений " + maxValueKey + " (встретилось " + sizeToFreq.get(maxValueKey) + " раз)");
        Map<Integer, Integer> sortedMap = sizeToFreq.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        System.out.println("Другие размеры:");
        sortedMap.entrySet().stream().forEach(entry -> {
            if (entry.getKey() != maxValueKey) {
                System.out.println("-" + entry.getKey() + " (" + entry.getValue() + " раз)");
            }
        });
    }

    public static int getSizeToFreqMaxValue() {
        int res = 0;
        if (!sizeToFreq.isEmpty()) {
            res = sizeToFreq.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
        }
        return res;
    }

    public static String generateRoute(String letters, int length) {
        int repetitionOfR = 0;
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char charToAdd = letters.charAt(random.nextInt(letters.length()));
            route.append(charToAdd);
            //route.append(letters.charAt(random.nextInt(letters.length())));
            if (charToAdd == 'R') {
                repetitionOfR++;
            }
        }
        synchronized (sizeToFreq) {
            if (sizeToFreq.containsKey(repetitionOfR)) {
                sizeToFreq.put(repetitionOfR, sizeToFreq.get(repetitionOfR) + 1);
            } else {
                sizeToFreq.put(repetitionOfR, 1);
            }
            sizeToFreq.notify();
        }
        return route.toString();
    }
}