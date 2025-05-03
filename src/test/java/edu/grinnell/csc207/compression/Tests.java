package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class Tests {
    @Test
    public void
    createFrequencyMapTest() throws IOException {
        Map<Short, Integer> fre = new HashMap<>();
        fre = Grin.createFrequencyMap("files/huffman-example.txt");

        assert(!fre.isEmpty());
        System.out.println(fre.toString());
        //HuffmanTree tr = new HuffmanTree(fre);

        /*
        Short bitsa = (short) 001100001;
        assert(fre.getOrDefault(bitsa, -1) == -1);
        Short bitsb = (short) 001100010;
        assert(fre.getOrDefault(bitsb, -1) == 2);
        Short bitsc = (short) 001100011;
        assert(fre.getOrDefault(bitsc, -1)== 0);
         */
        
    }
}
