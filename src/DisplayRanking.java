import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DisplayRanking extends JFrame {
    private int ranking;
    public JPanel panel;
    public JTextArea textArea;

    public DisplayRanking(ArrayList<Player> rankingForDisplay){
        setLayout(new FlowLayout());
        setTitle("Ranking");
        setSize(250,500);
        setBackground(Color.DARK_GRAY);
        setResizable(false);

        panel = new JPanel(new GridLayout(11,1,20,20));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel text = new JLabel("Ranking:");
        add(text);

        for (Player j: rankingForDisplay) {
            JLabel playerItem = new JLabel();
            playerItem.setText(rankingForDisplay.indexOf(j) + 1 + " º " + j.getNome() + " " + j.getPontuacao());
            panel.add(playerItem);
        }
        add(panel);
        setVisible(true);
        pack();
    }
}