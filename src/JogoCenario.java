
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;


import base.CenarioPadrao;
import base.Texto;

import static java.lang.Integer.parseInt;

public class JogoCenario extends CenarioPadrao {

    enum Estado {
        JOGANDO, GANHOU, PERDEU
    }

    private static final int ESPACAMENTO = 2;

    private Ranking ranking;

    private static final int ESPACO_VAZIO = -1;

    private static final int LINHA_COMPLETA = -2;

    private int largBloco, altBloco; // largura bloco e altura bloco

    private int ppx, ppy; // Posicao peca x e y

    private final int[][] grade = new int[10][16];

    private int temporizador = 0;

    private Texto texto = new Texto(20);

    private Random rand = new Random();

    private int idPeca = -1;
    private int idPrxPeca = -1;
    private Color corPeca;
    private int[][] peca;

    private int nivel = Jogo.nivel;
    private int pontos;
    private int linhasFeitas;

    private boolean animar;
    private boolean depurar;

    private Estado estado = Estado.JOGANDO;

    // Som
    private AudioInputStream as;

    private Clip clipAdicionarPeca;

    private Clip clipMarcarLinha;

    private Sequencer seqSomDeFundo;

    public JogoCenario(int largura, int altura) {
        super(largura, altura);
    }

    public Ranking getRanking() {
        return ranking;
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    @Override
    public void carregar() {
        largBloco = largura / grade.length;
        altBloco = altura / grade[0].length;

        for (int i = 0; i < grade.length; i++) {
            for (int j = 0; j < grade[0].length; j++) {
                grade[i][j] = ESPACO_VAZIO;
            }
        }

        Type[] audioFileTypes = AudioSystem.getAudioFileTypes();
        for (Type t : audioFileTypes) {
            System.out.println(t.getExtension());
        }

        try {

            as = AudioSystem.getAudioInputStream(new File("C:\\Users\\Lorde\\Documents\\Estudos\\Faculdade\\POO" +
                    "\\TetrisRemasterPOO\\src\\base\\som\\Cap05_som_adiciona_peca.wav"));

            clipAdicionarPeca = AudioSystem.getClip();
            clipAdicionarPeca.open(as);

            as = AudioSystem.getAudioInputStream(new File("C:\\Users\\Lorde\\Documents\\Estudos\\Faculdade\\POO" +
                    "\\TetrisRemasterPOO\\src\\base\\som\\Cap05_som_109662_grunz_success.wav"));
            clipMarcarLinha = AudioSystem.getClip();
            clipMarcarLinha.open(as);

            seqSomDeFundo = MidiSystem.getSequencer();
            seqSomDeFundo.setSequence(MidiSystem.getSequence(new File("C:\\Users\\Lorde\\Documents\\Estudos" +
                    "\\Faculdade\\POO\\TetrisRemasterPOO\\src\\base\\som\\Cap05_som_piano_quebrado.mid")));
            seqSomDeFundo.open();

            seqSomDeFundo.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);

            seqSomDeFundo.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        adicionaPeca();
    }

    @Override
    public void descarregar() {

        if (clipAdicionarPeca != null) {
            clipAdicionarPeca.stop();
            clipAdicionarPeca.close();
        }

        if (clipMarcarLinha != null) {
            clipMarcarLinha.stop();
            clipMarcarLinha.close();
        }

        if (seqSomDeFundo != null) {
            seqSomDeFundo.stop();
            seqSomDeFundo.close();
        }
    }

    @Override
    public void atualizar() throws IOException {

        if (estado != Estado.JOGANDO) {
            return;
        }

        if (Jogo.controleTecla[Jogo.Tecla.ESQUERDA.ordinal()]) {
            if (validaMovimento(peca, ppx - 1, ppy)) {
                ppx--;
            }

        } else if (Jogo.controleTecla[Jogo.Tecla.DIREITA.ordinal()]) {
            if (validaMovimento(peca, ppx + 1, ppy))
                ppx++;
        }

        if (Jogo.controleTecla[Jogo.Tecla.CIMA.ordinal()]) {
            girarReposicionarPeca(false);
        }
        else if (Jogo.controleTecla[Jogo.Tecla.BAIXO.ordinal()]) {
            if (validaMovimento(peca, ppx, ppy + 1)) {
                ppy++;
                pontos+=1;
            }
        }

        if (depurar && Jogo.controleTecla[Jogo.Tecla.BC.ordinal()]) {
            if (++idPeca == Peca.PECAS.length)
                idPeca = 0;

            peca = Peca.PECAS[idPeca];
            corPeca = Peca.Cores[idPeca];
        }

        Jogo.liberaTeclas();

        if (animar && temporizador >= 5) {
            animar = false;

            descerColunas();
            adicionaPeca();

        } else if (temporizador >= 20) {
            temporizador = 0;

            if (colidiu(ppx, ppy + 1)) {

                if (clipAdicionarPeca != null) {
                    clipAdicionarPeca.setFramePosition(0);
                    clipAdicionarPeca.start();
                }

                if (!parouForaDaGrade()) {
                    adicionarPecaNaGrade();
                    animar = marcarLinha();

                    peca = null;

                    if (!animar)
                        adicionaPeca();
                } else {
                    //QUANDO O JOGADOR PERDE, ENCERRA O JOGO AQUI
                    estado = Estado.PERDEU;
                    String nome = JOptionPane.showInputDialog(null,"Adicionar jogador ao Ranking","Adicionar",
                            JOptionPane.INFORMATION_MESSAGE);

                    if(nome != null){
                        Player newPlayer =  new Player(pontos,nome);
                        ranking = new Ranking();

                        //Checagem de Arquivos
                        File arquivoRanking = new File("C:\\Users\\Lorde\\Desktop\\TetrisRemasterPOOFinal" +
                                "\\Ranking" +
                                ".txt");

                        boolean state = arquivoRanking.createNewFile();

                        if (state){
                            System.out.println("Arquivo foi criado: " + arquivoRanking.getName());
                        } else {
                            System.out.println("Arquivo já presente: " + arquivoRanking.getName());
                        }

                        //Leitura do Arquivo e retenção dos dados no ranking do Programa
                        try(BufferedReader br = new BufferedReader(new FileReader(arquivoRanking))){
                            String linha = br.readLine();

                            while (linha != null){
                                String[] infoJogadores;

                                infoJogadores = linha.split(",");

                                int pontuacao = parseInt(infoJogadores[0]);

                                String nomeJogador = infoJogadores[1];

                                ranking.adicionaAoRanking(new Player(pontuacao, nomeJogador));

                                linha = br.readLine();
                            }
                        } catch (IOException e){
                            System.out.println("Erro do tipo: " + e.getMessage());
                        }

                        ranking.adicionaAoRanking(newPlayer);
                        ranking.removeDoRanking(Collections.max(ranking.rankingJogadores));

                        //Gravação no Arquivo com ranking já ordenado
                        try(BufferedWriter bw = new BufferedWriter(new FileWriter(arquivoRanking))){
                            for (Player j: ranking.rankingJogadores){
                                if(ranking.rankingJogadores.indexOf(j) < 10){
                                    if(j.getNome().isEmpty()){
                                        j.setNome("Desconhecido");
                                    }
                                    bw.write(j.getPontuacao() + "," + j.getNome());
                                    bw.newLine();
                                }
                            }
                        }
                        catch ( IOException e){
                            System.out.println("Erro do tipo: " + e.getMessage());
                        }
                        if(nome.isEmpty()){
                            JOptionPane.showMessageDialog(null,"Jogador Desconhecido com " + pontos + " pontos " +
                                            "adicionado" +
                                            " ao Ranking",
                                    "Adicionado com Sucesso",JOptionPane.INFORMATION_MESSAGE);

                        }
                        if(!nome.isEmpty()){
                            JOptionPane.showMessageDialog(null,"Jogador" + nome + " com " + pontos + " " +
                                            "pontos " +
                                            "adicionado" +
                                            " ao Ranking",
                                    "Adicionado com Sucesso",JOptionPane.INFORMATION_MESSAGE);
                        }
                        new DisplayRanking(ranking.rankingJogadores);
                    } else {
                        JOptionPane.showMessageDialog(null,"Perfil não Adicionado", "Não Adicionado",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            } else {
                ppy++;
            }
        } else {
            temporizador += nivel;
        }
    }

    public void adicionaPeca() {

        ppy = -2;
        ppx = grade.length / 2 - 1;

        // Primeira chamada
        if (idPeca == -1)
            idPeca = rand.nextInt(Peca.PECAS.length);
        else
            idPeca = idPrxPeca;
        // idPeca=6;
        idPrxPeca = rand.nextInt(Peca.PECAS.length);

        // Isso acontece muito
        if (idPeca == idPrxPeca)
            idPrxPeca = rand.nextInt(Peca.PECAS.length);

        peca = Peca.PECAS[idPeca];
        corPeca = Peca.Cores[idPeca];

    }

    private void adicionarPecaNaGrade() {

        for (int col = 0; col < peca.length; col++) {
            for (int lin = 0; lin < peca[col].length; lin++) {

                if (peca[lin][col] != 0) {

                    grade[col + ppx][lin + ppy] = idPeca;

                }
            }
        }
    }

    private boolean validaMovimento(int[][] peca, int px, int py) {

        if (peca == null)
            return false;

        for (int col = 0; col < peca.length; col++) {
            for (int lin = 0; lin < peca[col].length; lin++) {
                if (peca[lin][col] == 0)
                    continue;

                int prxPx = col + px; // Proxima posicao peca x
                int prxPy = lin + py; // Proxima posicao peca y

                if (prxPx < 0 || prxPx >= grade.length)
                    return false;

                if (prxPy >= grade[0].length)
                    return false;

                if (prxPy < 0)
                    continue;

                // Colidiu com uma peca na grade
                if (grade[prxPx][prxPy] > ESPACO_VAZIO)
                    return false;

            }
        }

        return true;
    }

    private boolean parouForaDaGrade() {

        if (peca == null)
            return false;

        for (int lin = 0; lin < peca.length; lin++) {
            for (int col = 0; col < peca[lin].length; col++) {
                if (peca[lin][col] == 0)
                    continue;
                // Fora da grade
                if (lin + ppy < 0)
                    return true;
            }
        }

        return false;
    }

    private boolean colidiu(int px, int py) {

        if (peca == null)
            return false;

        for (int col = 0; col < peca.length; col++) {
            for (int lin = 0; lin < peca[col].length; lin++) {
                if (peca[lin][col] == 0)
                    continue;

                int prxPx = col + px;
                int prxPy = lin + py;

                if (depurar) {
                    if (prxPx < 0 || prxPx >= grade.length)
                        return false;
                }
                // Chegou na base da grade
                if (prxPy == grade[0].length)
                    return true;

                // Fora da grade
                if (prxPy < 0)
                    continue;

                // Colidiu com uma peca na grade
                if (grade[prxPx][prxPy] > ESPACO_VAZIO)
                    return true;
            }
        }

        return false;
    }

    private boolean marcarLinha() {

        int contadorLinhas = 0;

        int[] pontuacao = new int[5];

        pontuacao[0] = 0;
        pontuacao[1] = 100;
        pontuacao[2] = 300;
        pontuacao[3] = 500;
        pontuacao[4] = 800;

        for (int lin = grade[0].length - 1; lin >= 0; lin--) {

            boolean linhaCompleta = true;

            for (int col = grade.length - 1; col >= 0; col--) {
                if (grade[col][lin] == ESPACO_VAZIO) {
                    linhaCompleta = false;
                    break;
                }
            }

            if (linhaCompleta) {
                linhasFeitas++;
                contadorLinhas++;
                for (int col = grade.length - 1; col >= 0; col--) {
                    grade[col][lin] = LINHA_COMPLETA;
                }
            }
        }

        pontos += pontuacao[contadorLinhas] * Jogo.getNivel();

        if (linhasFeitas == 10) {
            estado = Estado.JOGANDO;
            nivel++;
            linhasFeitas = 0;
        }

        return contadorLinhas>0;
    }

    private void descerColunas() {
        for (int col = 0; col < grade.length; col++) {
            for (int lin = grade[0].length - 1; lin >= 0; lin--) {

                if (grade[col][lin] == LINHA_COMPLETA) {
                    int moverPara = lin;
                    int prxLinha = lin - 1;

                    for (; prxLinha > -1; prxLinha--) {
                        if (grade[col][prxLinha] == LINHA_COMPLETA)
                            continue;
                        else
                            break;

                    }

                    for (; moverPara > -1; moverPara--, prxLinha--) {

                        if (prxLinha > -1)
                            grade[col][moverPara] = grade[col][prxLinha];
                        else
                            grade[col][moverPara] = ESPACO_VAZIO;

                    }
                }
            }
        }

        if (clipMarcarLinha != null) {
            clipMarcarLinha.setFramePosition(0);
            clipMarcarLinha.start();
        }

    }

    protected void girarPeca(boolean sentidoHorario) {
        if (peca == null)
            return;

        final int[][] temp = new int[peca.length][peca.length];

        for (int i = 0; i < peca.length; i++) {
            for (int j = 0; j < peca.length; j++) {
                if (sentidoHorario)
                    temp[j][peca.length - i - 1] = peca[i][j];
                else
                    temp[peca.length - j - 1][i] = peca[i][j];
            }
        }

        System.out.println("Antes:");
        imprimirArray(peca);
        System.out.println("Depois:");
        imprimirArray(temp);

        if (validaMovimento(temp, ppx, ppy)) {
            peca = temp;
        }
    }

    private void imprimirArray(int[][] arr) {
        for (int lin = 0; lin < arr.length; lin++) {
            for (int col = 0; col < arr[lin].length; col++) {
                System.out.print(arr[lin][col] + "\t");
            }

            System.out.println();
        }
    }

    private void girarReposicionarPeca(boolean sentidoHorario) {
        if (peca == null)
            return;

        int tempPx = ppx;
        final int[][] tempPeca = new int[peca.length][peca.length];

        for (int i = 0; i < peca.length; i++) {
            for (int j = 0; j < peca.length; j++) {
                if (sentidoHorario)
                    tempPeca[j][peca.length - i - 1] = peca[i][j];
                else
                    tempPeca[peca.length - j - 1][i] = peca[i][j];
            }
        }

        // Reposiciona peca na tela
        for (int i = 0; i < tempPeca.length; i++) {
            for (int j = 0; j < tempPeca.length; j++) {
                if (tempPeca[j][i] == 0) {
                    continue;
                }

                int prxPx = i + tempPx;

                if (prxPx < 0)
                    tempPx = tempPx - prxPx;

                else if (prxPx == grade.length)
                    tempPx = tempPx - 1;

            }
        }

        if (validaMovimento(tempPeca, tempPx, ppy)) {
            peca = tempPeca;
            ppx = tempPx;
        }
    }

    @Override
    public void desenhar(Graphics2D g) {

        for (int col = 0; col < grade.length; col++) {
            for (int lin = 0; lin < grade[0].length; lin++) {
                int valor = grade[col][lin];

                if (valor == ESPACO_VAZIO)
                    continue;

                if (valor == LINHA_COMPLETA)
                    g.setColor(Color.RED);
                else
                    g.setColor(Peca.Cores[valor]);

                int x = col * largBloco + ESPACAMENTO;
                int y = lin * altBloco + ESPACAMENTO;

                g.fillRect(x, y, largBloco - ESPACAMENTO, altBloco - ESPACAMENTO);

            }
        }

        if (peca != null) {
            g.setColor(corPeca);

            for (int col = 0; col < peca.length; col++) {
                for (int lin = 0; lin < peca[col].length; lin++) {
                    if (peca[lin][col] != 0) {

                        int x = (col + ppx) * largBloco + ESPACAMENTO;
                        int y = (lin + ppy) * altBloco + ESPACAMENTO;

                        g.fillRect(x, y, largBloco - ESPACAMENTO, altBloco - ESPACAMENTO);

                    } else if (depurar) {
                        g.setColor(Color.PINK);
                        int x = (col + ppx) * largBloco + ESPACAMENTO;
                        int y = (lin + ppy) * altBloco + ESPACAMENTO;

                        g.fillRect(x, y, largBloco - ESPACAMENTO, altBloco - ESPACAMENTO);

                        g.setColor(corPeca);
                    }
                }
            }
        }

        int miniatura = largBloco / 4;
        int[][] prxPeca = Peca.PECAS[idPrxPeca];
        g.setColor(Peca.Cores[idPrxPeca]);

        for (int col = 0; col < prxPeca.length; col++) {
            for (int lin = 0; lin < prxPeca[col].length; lin++) {
                if (prxPeca[lin][col] == 0)
                    continue;

                int x = col * miniatura + ESPACAMENTO;
                int y = lin * miniatura + ESPACAMENTO;

                g.fillRect(x, y, miniatura - ESPACAMENTO, miniatura - ESPACAMENTO);

            }
        }

        texto.setCor(Color.WHITE);
        texto.desenha(g, "Level " + nivel + " - " + linhasFeitas, largura / 2 - 20, 20);
        texto.desenha(g, String.valueOf(pontos), largura - 50, 20);

        if (estado != Estado.JOGANDO) {
            texto.setCor(Color.WHITE);
            if (estado == Estado.GANHOU) {
                texto.desenha(g, "Finalmente!", 180, 180);
            }
            else {
                texto.desenha(g, "Deu ruim!", 180, 180);
            }
        }
    }

}
