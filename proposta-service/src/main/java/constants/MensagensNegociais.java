package constants;

public class MensagensNegociais {
    private MensagensNegociais(){}

    public static final String CPF_OBRIGATORIO = "É obrigatório informar o CPF do cliente.";

    public static final String VALOR_IMOVEL_OBRIGATORIO = "É obrigatório informar o valor do imóvel.";
    public static final String VALOR_IMOVEL_MAIOR_QUE_ZERO = "O valor do imóvel deve ser maior que zero.";
    
    public static final String VALOR_ENTRADA_OBRIGATORIO = "É obrigatório informar o valor de entrada.";
    public static final String VALOR_ENTRADA_MAIOR_QUE_ZERO = "O valor da entrada deve ser maior que zero.";
    public static final String VALOR_ENTRADA_MAIOR_QUE_VALOR_IMOVEL = "O valor de entrada não pode ser maior que o valor do imóvel.";
    public static final String VALOR_ENTRADA_MENOR_QUE_20_POR_CENTO = "O valor de entrada não pode ser menor que 20% do valor do imóvel.";
    
    public static final String QUANTIDADE_PARCELAS_OBRIGATORIO = "É obrigatório informar a quantidade de parcelas.";
    public static final String QUANTIDADE_PARCELAS_MAIOR_QUE_360 = "A quantidade de parcelas não pode ser maior que 360.";

    public static final String CLIENTE_NAO_ENCONTRADO = "Cliente não encontrado.";
    public static final String CLIENTE_NAO_ELEGIVEL = "Cliente não elegível para a proposta.";
}
