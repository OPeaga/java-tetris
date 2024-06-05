import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class inputForm {
    private JButton cancelButton;
    private JButton okButton;
    private JTextField inputName;
    private JLabel labelName;
    private String nome;

    public inputForm(int pontos){
        JFrame frame = new JFrame();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Adicionar ao Ranking");
        frame.setSize(400,500);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel.setLayout(new GridLayout(2,3,20,20));
        frame.setVisible(true);

        JLabel labelName = new JLabel("Nome", SwingConstants.CENTER);
        panel.add(labelName);

        JTextField labelInput = new JTextField();
        labelInput.setToolTipText("Coloque seu nome");
        panel.add(labelInput);

        JButton okButton = new JButton("Ok");
        panel.add(okButton);

        JButton reiniciarButton = new JButton("Cancel");
        panel.add(reiniciarButton);

        frame.setContentPane(panel);
        frame.setSize(400,230);
        frame.pack();
        frame.setVisible(true);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"Jogador: " + inputName.getText() + " com " + pontos + " pontos",
                        "Adicionado com Sucesso",JOptionPane.INFORMATION_MESSAGE);
                frame.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public static void main(String[] args) {
        new inputForm(20);
    }
}
