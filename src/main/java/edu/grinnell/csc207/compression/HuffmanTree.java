package edu.grinnell.csc207.compression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally
 * take 8 bits.  However, we also need to encode a special EOF character to
 * denote the end of a .grin file.  Thus, we need 9 bits to store each
 * byte value.  This is fine for file writing (modulo the need to write in
 * byte chunks to the file), but Java does not have a 9-bit data type.
 * Instead, we use the next larger primitive integral type, short, to store
 * our byte values.
 */
public class HuffmanTree {
    /**
     * A node of the binary search tree.
     */
    private static class Node implements Comparable<Node> {
        int freq;
        short ch;
        Node left;
        Node right;

        /**
         * @param freq the frequency of the ch
         * @param ch the value of the node
         * @param left the left child of the node
         * @param right the right child of the node
         */
        Node(int freq, short ch, Node left, Node right) {
            this.freq = freq;
            this.ch = ch;
            this.left = left;
            this.right = right;
        }

        /**
         * @param freq the frequency of the ch
         * @param ch the value of the node
         */
        Node(int freq, short ch) {
            this(freq, ch, null, null);
        }

        @Override
        public int compareTo(Node node) {
            int comparison = Integer.compare(this.freq, node.freq);
            if (comparison != 0) {
                return comparison;
            }
            return Short.compare(this.ch, node.ch);            
        }
    }

    private Node root;

    /**
     * Constructs a new HuffmanTree from a frequency map.
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree(Map<Short, Integer> freqs) {
        freqs.put((short) 256, 1);
        Set<Short> keys = new HashSet<>(freqs.keySet());
        PriorityQueue<Node> queue = new PriorityQueue<>();
        for (Short key: keys) { // Makes a new Node for each key in the list of keys in map
            int freq = freqs.get(key);
            Node newNode = new Node(freq, key);
            queue.add(newNode); // And puts node into a priority queue.
        }
        while (queue.size() > 1) {
            Node leaf = new Node(0, (short) 0);
            leaf.left = queue.peek();
            int lFreq = queue.poll().freq;
            leaf.right = queue.peek();
            int rFreq = queue.poll().freq;
            leaf.freq = lFreq + rFreq;
            queue.add(leaf);
        }
        root = queue.poll();        
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree(BitInputStream in) { //assuming intputStream starts at the serialized tree 
        root = huffmanH(root, in);
    }

    /**
     * Returns a new node that is either a leaf or subtree of the serialized HuffmanTree
     * @param root
     * @param in
     * @return root a new node that is either a leaf or subtree
     */
    private Node huffmanH(Node root, BitInputStream in) {
        root = new Node(0, (short) 0);
        int nodeLeaf = in.readBit();
        if (nodeLeaf == 1) {
            root.left = huffmanH(root.left, in);
            root.right = huffmanH(root.right, in);
        } else if (nodeLeaf == 0) {
            root.ch = (short) in.readBits(9);
        }
        return root;
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * @param out the output file as a BitOutputStream
     */
    public void serialize(BitOutputStream out) {
        serializeH(root, out);
    }

    /**
     * @param root the current node of the recursion
     * @param out
     */
    private void serializeH(Node root, BitOutputStream out) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {
            out.writeBit(0);
            out.writeBits(root.ch, 9);
        } else {
            out.writeBit(1);
            serializeH(root.left, out);
            out.writeBit(1);
            serializeH(root.right, out);
        }
    }
   
    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * @param in the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode(BitInputStream in, BitOutputStream out) {
        while (in.hasBits()) {
            Short find = (short) in.readBits(8);

            List<String> path = findPath(find);
            //Next parse the list of Strings to write path to node
            //System.out.println(path.isEmpty());
            while (!path.isEmpty()) {
                String direction = path.remove(0);
                if (direction.equals("l")) {
                    out.writeBit(0);
                } else {
                    out.writeBit(1);
                }
            }
            out.writeBits(256, 9);
        }
    }

    /**
     * takes in a ch to find and returns a list of strings of the final path taken
     * @param find
     * @return path a list of the path in the form of l or r Strings 
     */
    private List<String> findPath(short find) {
        List<String> path = new ArrayList<>();
        return findPathH(find, path, root, false);
    }

    /**
     * adds the direction "l" "r" to the path while recursing through the tree searching for find  
     * @param find
     * @param path
     * @param root
     * @param found
     * @return path the path taken relative to the root
     */
    private List<String> findPathH(short find, List<String> path, Node root, boolean found) {
        if (root.ch == find) {
            found = true;
            return path;
        } 
        if (root.left != null) {
            path.add("l");
            path = findPathH(find, path, root.left, found);
            if (!found) {
                path.remove(path.size() - 1); 
                //The last element should always be the one added a few lines up
            }
        } 
        if (root.right != null) {
            path.add("r");
            path = findPathH(find, path, root.right, found);
            if (!found) {
                path.remove(path.size() - 1); 
                //The last element should always be the one added a few lines up
            }
        }
        return path;
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of
     * bits into their uncompressed form, saving the results to the given
     * output stream. Note that the EOF character is not written to out
     * because it is not a valid 8-bit chunk (it is 9 bits).
     * @param in the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode(BitInputStream in, BitOutputStream out) {
        //Assumes in is now at the payload portion
        Node curRoot = root;
        while (in.hasBits()) {
            int curBit = in.readBit(); 
            if (curBit == 1 && curRoot.right != null) {
                curRoot = curRoot.right;
            } else if (curBit == 0 && curRoot.left != null) {
                curRoot = curRoot.left;
            }
            if (curRoot.left == null && curRoot.ch != 256) {
                out.writeBits(curRoot.ch, 8);
                curRoot = root;
            }
        }
    }
}
