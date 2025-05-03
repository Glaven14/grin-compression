package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The driver for the Grin compression program.
 */
public class Grin {
    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * @param infile the file to decode
     * @param outfile the file to ouptut to
     * @throws IOException 
     */
    public static void decode(String infile, String outfile) throws IOException {
        BitInputStream intake = new BitInputStream(infile);
        BitOutputStream output = new BitOutputStream(outfile);
        int magicNum = intake.readBits(32);
        if (magicNum == 1846) {
            //output.writeBits(magicNum, 8);
            HuffmanTree decoTree = new HuffmanTree(intake);
            decoTree.decode(intake, output);
            intake.close();
            output.close();
        } else {
            throw new IllegalArgumentException("Please enter a .grin file to decode");
        }
    }


    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of
     * those sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     * @param file the file to read
     * @return a freqency map for the given file
     * @throws IOException 
     */
    public static Map<Short, Integer> createFrequencyMap(String file) throws IOException {
        BitInputStream intake = new BitInputStream(file);
        Map<Short, Integer> freq = new HashMap<Short, Integer>();
        while (intake.hasBits()) {
            Short bits = (short) intake.readBits(8);
            if (bits != -1) {
                freq.putIfAbsent(bits, 0);            //Makes sure entry is in for bits
                freq.put(bits, (freq.get(bits) + 1)); //Adds 1 to the frequency of bits.
            }
        }
        return freq;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * @param infile the file to encode.
     * @param outfile the file to write the output to.
     * @throws IOException 
     */
    public static void encode(String infile, String outfile) throws IOException {
        Map<Short, Integer> freq = new HashMap<Short, Integer>();
        freq = createFrequencyMap(infile);
        HuffmanTree tree = new HuffmanTree(freq);
        BitInputStream intake = new BitInputStream(infile);
        BitOutputStream output = new BitOutputStream(outfile);
        output.writeBits(1846, 32);
        tree.serialize(output);
        tree.encode(intake, output);
        intake.close();
        output.close();
    }

    /**
     * The entry point to the program.
     * @param args the command-line arguments.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
        } else {
            if (!(args[0].equals("encode") || args[0].equals("decode"))) {
                System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
            } else {
                if (args[0].equals("decode")) {
                    decode(args[1], args[2]);
                } else {
                    encode(args[1], args[2]);
                }
            }
        }
    }
}
