import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import static java.lang.Integer.parseInt;

public class Ranking{
    ArrayList<Player> rankingJogadores = new ArrayList<>();

    public void adicionaAoRanking(Player p){
        rankingJogadores.add(p);
        Collections.sort(rankingJogadores);
    }
    public void removeDoRanking(Player p){
        rankingJogadores.remove(p);
        Collections.sort(rankingJogadores);
    }
    public void limpaRanking(){
        rankingJogadores.clear();
        //Gravar em TXT o resultado
    }
    public void imprimeRanking(){
        for (Player j: rankingJogadores) {
            if(rankingJogadores.indexOf(j) < 10){
                System.out.printf(rankingJogadores.indexOf(j) + 1 + "º ");
                j.imprimeDados();
            }
        }
    }


}
