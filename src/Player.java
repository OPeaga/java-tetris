public class Player implements Comparable<Player> {
    private int pontuacao;
    private String nome;
    public Player(int pontuacao, String nome) {
        this.pontuacao = pontuacao;
        this.nome = nome;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public int compareTo(Player outro) {
        Integer pontuacao1 = this.pontuacao;
        Integer pontuacao2 = outro.pontuacao;
        //Rankin by Points
        int comp = pontuacao1.compareTo(pontuacao2);
        if(comp == 0){
            return 0;
        } else {
            return comp*(-1);
        }
    }
    public void imprimeDados(){
        System.out.printf("%s --- %d pontos\n",this.nome, this.pontuacao);
    }
}
