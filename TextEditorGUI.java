// 21k-3902 Muzammil Ahmed
// 21k-3856 Abdurr Rafay 
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

class Node {
    String line;
    Node next;

    public Node(String line) {
        this.line = line;
        this.next = null;
    }
}
class TextEditor {
    private Node head;

    public TextEditor() {
        this.head = null;
    }
    public void insert(String line, int lineNumber) {
        //method to add text at specific line number
        Node newNode = new Node(line);
        if (head == null) {
            head = newNode;
            return;
        }
        if (lineNumber == 1) {
            newNode.next = head;
            head = newNode;
            return;
        }
        Node currentNode = head;
        int currentLineNumber = 1;
        while (currentNode != null) {
            if (currentLineNumber == lineNumber - 1) {
                newNode.next = currentNode.next;
                currentNode.next = newNode;
                return;
            }
            currentNode = currentNode.next;
            currentLineNumber++;
        }

    }

    public void delete(int lineNumber) {
        if (head == null) {
            System.out.println("Text editor is empty");
            return;
        }
        if (lineNumber == 1) {
            head = head.next;
            return;
        }
        Node currentNode = head;
        int currentLineNumber = 1;
        while (currentNode != null) {
            //The lineNumber - 1 is because we want to delete the line after the line number
            if (currentLineNumber == lineNumber - 1) {
                currentNode.next = currentNode.next.next;
                return;
            }
            currentNode = currentNode.next;
            currentLineNumber++;
        }
        System.out.println("Line number out of range");
    }

    public void load(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            while (line != null) {
                insert(line, Integer.MAX_VALUE);
                line = reader.readLine();
            }
        }
    }
    public void save(String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            Node currentNode = head;
            while (currentNode != null) {
                writer.write(currentNode.line + "\n");
                currentNode = currentNode.next;
            }
        }
    }
    public String getText() {
        String text = "";
        Node currentNode = head;
        while (currentNode != null) {
            text += currentNode.line + "\n";
            currentNode = currentNode.next;
        }
        return text;
    }
}

public class TextEditorGUI extends JFrame {
    private JTextArea textArea;
    private TextEditor textEditor;
    private Stack<String> undoStack;
    private BinarySearchTree dictionary;

    public TextEditorGUI() {
        super("Text Editor");
        textEditor = new TextEditor();
        undoStack = new Stack<>();
        initializeUI();
        dictionary = new BinarySearchTree();
    }

    private void initializeUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem newMenuItem = new JMenuItem("New");
        newMenuItem.addActionListener(new NewActionListener());
        fileMenu.add(newMenuItem);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(new OpenActionListener());
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(new SaveActionListener());
        fileMenu.add(saveMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ExitActionListener());
        fileMenu.add(exitMenuItem);

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        JMenuItem insertMenuItem = new JMenuItem("Insert");
        insertMenuItem.addActionListener(new InsertActionListener());
        editMenu.add(insertMenuItem);

        JMenuItem findMenuItem = new JMenuItem("Find");
        findMenuItem.addActionListener(new FindActionListener());
        editMenu.add(findMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.addActionListener(new DeleteActionListener());
        editMenu.add(deleteMenuItem);

        textArea.getDocument().addUndoableEditListener(e -> {
            undoStack.push(textArea.getText());
        });
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.addActionListener(new UndoActionListener());
        editMenu.add(undoMenuItem);

        JMenu dictionaryMenu = new JMenu("Dictionary");
        menuBar.add(dictionaryMenu);

        JMenuItem dictionaryMenuItem = new JMenuItem("Add to Dictionary");
        dictionaryMenuItem.addActionListener(new AddToDictionary());
        dictionaryMenu.add(dictionaryMenuItem);

        JMenuItem dictionaryDeleteMenuItem = new JMenuItem("Delete from Dictionary");
        dictionaryDeleteMenuItem.addActionListener(new deleteFromDictionary());
        dictionaryMenu.add(dictionaryDeleteMenuItem);
        setVisible(true);
    }

    private void performUndo() {
        if (!undoStack.isEmpty()) {
            String previousState = undoStack.pop();
            if (!previousState.isEmpty()) {
                String updatedState = previousState.substring(0, previousState.length() - 1);
                textArea.setText(updatedState);
            }
        }
    }
    private class NewActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            textArea.setText("");
            textEditor = new TextEditor();
        }
    }

    private class UndoActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            performUndo();
        }
    }
    private class OpenActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Text Files", "txt", "text");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    textEditor.load(chooser.getSelectedFile().getPath());
                    BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile().getPath()));
                    String line = reader.readLine();
                    while (line != null) {
                        textArea.append(line + "\n");
                        line = reader.readLine();
                    }
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    private class SaveActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String text = textArea.getText();
            textEditor.insert(text, Integer.MAX_VALUE);


            try {

                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Text Files", "txt", "text");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to save this file: " +
                            chooser.getSelectedFile().getName());
                }
                textEditor.save(chooser.getSelectedFile().getPath());

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ExitActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private class InsertActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String inputLineNumber = JOptionPane.showInputDialog("Enter line number to insert at:");
            int lineNumber = Integer.parseInt(inputLineNumber);
            String inputText = JOptionPane.showInputDialog("Enter text to insert:");

            textEditor.insert(inputText, lineNumber);
            updateTextArea();
        }

        private void updateTextArea() {
            textArea.setText(textEditor.getText());
        }
    }
    class FindActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String searchString = JOptionPane.showInputDialog("Enter search text:");
            if (searchString != null && !searchString.isEmpty()) {
                String text = textArea.getText();
                Highlighter highlighter = textArea.getHighlighter();
                highlighter.removeAllHighlights();
                int index = text.indexOf(searchString);
                while (index >= 0) {
                    try {
                        highlighter.addHighlight(index, index + searchString.length(),
                                new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                        index = text.indexOf(searchString, index + searchString.length());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    private class DeleteActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String input = JOptionPane.showInputDialog("Enter line number to delete:");
            int lineNumber = Integer.parseInt(input);
            textEditor.delete(lineNumber);
            String[] lines = textArea.getText().split("\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                if (i != lineNumber - 1) {
                    sb.append(lines[i] + "\n");
                }
            }
            textArea.setText(sb.toString());
        }
    }

    private class AddToDictionary implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String searchString = JOptionPane.showInputDialog("Enter word to add:");
            if (searchString != null && !searchString.isEmpty()) {
                String text = textArea.getText();
                Highlighter highlighter = textArea.getHighlighter();
                highlighter.removeAllHighlights();
                int index = text.indexOf(searchString);
                while (index >= 0) {
                    try {
                        highlighter.addHighlight(index, index + searchString.length(),
                                new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                        index = text.indexOf(searchString, index + searchString.length());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // System.out.println(searchString);
            dictionary.add(searchString);
            dictionary.writeToFile("dictionary.txt");

            dictionary.inOrderTraversal();
            System.out.println();
            dictionary.postOrderTraversal();
            System.out.println();
            dictionary.preOrderTraversal();
            System.out.println();
        }
    }
    private class deleteFromDictionary implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String searchString = JOptionPane.showInputDialog("Enter word to delete:");
            if (searchString != null && !searchString.isEmpty()) {
                String text = textArea.getText();
                Highlighter highlighter = textArea.getHighlighter();
                highlighter.removeAllHighlights();
                int index = text.indexOf(searchString);
                while (index >= 0) {
                    try {
                        highlighter.addHighlight(index, index + searchString.length(),
                                new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                        index = text.indexOf(searchString, index + searchString.length());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // System.out.println(searchString);
            dictionary.deleteNodeFromFile(searchString, "dictionary.txt");

            dictionary.inOrderTraversal();
            System.out.println();
            dictionary.postOrderTraversal();
            System.out.println();
            dictionary.preOrderTraversal();
            System.out.println();
        }
    }
    public static void main(String[] args) {

        TextEditorGUI editor = new TextEditorGUI();
    }
}
class BinarySearchTree {
    public Node root;

    private class Node {
        String data;
        Node left;
        Node right;

        public Node(String data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    public void add(String data) {
        root = addRecursive(root, data);
    }

    private Node addRecursive(Node current, String data) {
        if (current == null) {
            return new Node(data);
        }

        if (data.compareTo(current.data) < 0) {
            current.left = addRecursive(current.left, data);
        } else if (data.compareTo(current.data) > 0) {
            current.right = addRecursive(current.right, data);
        }

        return current;
    }

    public void remove(String data) {
        root = removeRecursive(root, data);
    }

    private Node removeRecursive(Node current, String data) {
        if (current == null) {
            return null;
        }

        if (data.equals(current.data)) {
            if (current.left == null && current.right == null) {
                return null;
            }
            if (current.left == null) {
                return current.right;
            }
            if (current.right == null) {
                return current.left;
            }
            String smallestValue = findSmallestValue(current.right);
            current.data = smallestValue;
            current.right = removeRecursive(current.right, smallestValue);
            return current;
        }

        if (data.compareTo(current.data) < 0) {
            current.left = removeRecursive(current.left, data);
            return current;
        }

        current.right = removeRecursive(current.right, data);
        return current;
    }

    private String findSmallestValue(Node root) {
        return root.left == null ? root.data : findSmallestValue(root.left);
    }

    public boolean contains(String data) {
        return containsRecursive(root, data);
    }

    private boolean containsRecursive(Node current, String data) {
        if (current == null) {
            return false;
        }

        if (data.equals(current.data)) {
            return true;
        }

        if (data.compareTo(current.data) < 0) {
            return containsRecursive(current.left, data);
        }

        return containsRecursive(current.right, data);
    }

    public void inOrderTraversal() {
        inOrderTraversalRecursive(root);
    }
    private void inOrderTraversalRecursive(Node current) {
        if (current != null) {
            inOrderTraversalRecursive(current.left);
            System.out.print(current.data + " ");
            inOrderTraversalRecursive(current.right);
        }
    }

    public void preOrderTraversal() {
        preOrderTraversalRecursive(root);
    }

    private void preOrderTraversalRecursive(Node current) {
        if (current != null) {
            System.out.print(current.data + " ");
            preOrderTraversalRecursive(current.left);
            preOrderTraversalRecursive(current.right);
        }
    }

    public void postOrderTraversal() {
        postOrderTraversalRecursive(root);
    }
    private void postOrderTraversalRecursive(Node current) {
        if (current != null) {
            postOrderTraversalRecursive(current.left);
            postOrderTraversalRecursive(current.right);
            System.out.print(current.data + " ");
        }
    }
    // public void writeToFile(Node root, String fileName) throws IOException {
    // File file = new File("dictionary.txt");
    // BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    // writeNode(root, writer);
    // writer.close();
    // }
    public void writeToFile(String filename) {
        try {
            FileWriter writer = new FileWriter(new File(filename));
            writeToFile(root, writer);
            writer.close();
            System.out.println("Data written to file successfully.");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
    private void writeToFile(Node node, FileWriter writer) throws IOException {
        if (node == null) {
            return;
        }
        writeToFile(node.left, writer);
        writer.write(node.data + "\n");
        writeToFile(node.right, writer);
    }
    private void writeNode(Node node, BufferedWriter writer) throws IOException {
        if (node == null) {
            return;
        }
        writeNode(node.left, writer);
        writer.write(node.data + "\n");
        writeNode(node.right, writer);
    }
    public void deleteNodeFromFile(String dataToDelete, String filename) {
        // Search for the node to delete
        Node current = root;
        Node parent = null;
        boolean isLeft = false;
        while (current != null) {
            if (current.data.equals(dataToDelete)) {
                break;
            } else {
                parent = current;
                if (dataToDelete.compareTo(current.data) < 0) {
                    current = current.left;
                    isLeft = true;
                } else {
                    current = current.right;
                    isLeft = false;
                }
            }
        }
        // If node is not found, return
        if (current == null) {
            System.out.println("Node not found.");
            return;
        }
        // If node has no children
        if (current.left == null && current.right == null) {
            if (current == root) {
                root = null;
            } else if (isLeft) {
                parent.left = null;
            } else {
                parent.right = null;
            }
        }
        // If node has only one child
        else if (current.right == null) {
            if (current == root) {
                root = current.left;
            } else if (isLeft) {
                parent.left = current.left;
            } else {
                parent.right = current.left;
            }
        } else if (current.left == null) {
            if (current == root) {
                root = current.right;
            } else if (isLeft) {
                parent.left = current.right;
            } else {
                parent.right = current.right;
            }
        }
        // If node has two children
        else {
            // Find the minimum element in the right subtree
            Node successor = getSuccessor(current);
            // Connect parent of current to successor instead
            if (current == root) {
                root = successor;
            } else if (isLeft) {
                parent.left = successor;
            } else {
                parent.right = successor;
            }
            // Connect successor to current's left child
            successor.left = current.left;
        }
        // Write the updated tree to file
        writeToFile(filename);
    }
    private Node getSuccessor(Node node) {
        Node successorParent = node;
        Node successor = node;
        Node current = node.right;
        // Find the minimum element in the right subtree
        while (current != null) {
            successorParent = successor;
            successor = current;
            current = current.left;
        }
        // If successor is not the right child, then connect successor's right child to
        // successor's parent
        if (successor != node.right) {
            successorParent.left = successor.right;
            successor.right = node.right;
        }
        return successor;
    }
}