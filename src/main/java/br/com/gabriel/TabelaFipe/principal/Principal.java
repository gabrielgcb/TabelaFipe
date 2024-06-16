package br.com.gabriel.TabelaFipe.principal;

import br.com.gabriel.TabelaFipe.model.Dados;
import br.com.gabriel.TabelaFipe.model.Modelos;
import br.com.gabriel.TabelaFipe.model.Veiculo;
import br.com.gabriel.TabelaFipe.service.ConsumoApi;
import br.com.gabriel.TabelaFipe.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner scan = new Scanner(System.in);
    private final String URL_BASE = "https://parallelum.com.br/fipe/api/v1/";
    private final ConsumoApi consumoApi = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    public static void main(String[] args) {
        Principal principal = new Principal();
        principal.exibirMenu();
    }

    public void exibirMenu() {
        String opcao = obterOpcaoMenu();

        String endereco = escolherTipoVeiculo(opcao);
        List<Dados> marcas = obterDadosMarcas(endereco);
        exibirMarcas(marcas);

        int codigoMarca = obterCodigoMarca();
        endereco = endereco + "/" + codigoMarca + "/modelos";
        Modelos modelos = obterDadosModelos(endereco);
        exibirModelos(modelos.modelos());

        String nomeModelo = obterNomeModelo();
        List<Dados> modelosFiltrados = filtrarModelosPorNome(modelos.modelos(), nomeModelo);
        exibirModelosFiltrados(modelosFiltrados);

        int codigoModelo = obterCodigoModelo();
        List<Veiculo> veiculos = obterDadosVeiculos(endereco, codigoModelo);
        exibirVeiculos(veiculos);
    }

    private String obterOpcaoMenu() {
        String menu = """
                *** OPÇÕES ***
                Carro
                Moto
                Caminhão
                
                Digite uma das opções para consulta:\t """;
        System.out.println(menu);
        return scan.nextLine();
    }

    private String escolherTipoVeiculo(String opcao) {
        if (opcao.toLowerCase().contains("carr")) {
            return URL_BASE + "carros/marcas";
        } else if (opcao.toLowerCase().contains("mot")) {
            return URL_BASE + "motos/marcas";
        } else {
            return URL_BASE + "caminhoes/marcas";
        }
    }

    private List<Dados> obterDadosMarcas(String endereco) {
        String json = consumoApi.obterDados(endereco);
        return conversor.obterLista(json, Dados.class);
    }

    private void exibirMarcas(List<Dados> marcas) {
        marcas.stream()
                .sorted(Comparator.comparing(Dados::nome))
                .forEach(System.out::println);
    }

    private int obterCodigoMarca() {
        System.out.println("\nEscolha o código da marca do veículo a ser consultado: ");
        int codigo = scan.nextInt();
        scan.nextLine();  // Consumir o newline deixado pelo nextInt()
        return codigo;
    }

    private Modelos obterDadosModelos(String endereco) {
        String json = consumoApi.obterDados(endereco);
        return conversor.obterDados(json, Modelos.class);
    }

    private void exibirModelos(List<Dados> modelos) {
        System.out.println("\nModelos dessa marca: ");
        modelos.stream()
                .sorted(Comparator.comparing(Dados::nome))
                .forEach(System.out::println);
    }

    private String obterNomeModelo() {
        System.out.println("\nDigite um trecho do nome do modelo a ser buscado: ");
        return scan.nextLine();
    }

    private List<Dados> filtrarModelosPorNome(List<Dados> modelos, String nomeModelo) {
        return modelos.stream()
                .filter(m -> m.nome().toLowerCase().contains(nomeModelo.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void exibirModelosFiltrados(List<Dados> modelosFiltrados) {
        System.out.println("\nModelos filtrados: ");
        modelosFiltrados.forEach(System.out::println);
    }

    private int obterCodigoModelo() {
        System.out.println("\nDigite o código do modelo para buscar os valores da avaliação: ");
        int codigoModelo = scan.nextInt();
        scan.nextLine();  // Consumir o newline deixado pelo nextInt()
        return codigoModelo;
    }

    private List<Veiculo> obterDadosVeiculos(String enderecoBase, int codigoModelo) {
        String endereco = enderecoBase + "/" + codigoModelo + "/" + "anos";
        String json = consumoApi.obterDados(endereco);
        List<Dados> anosAvaliacao = conversor.obterLista(json, Dados.class);
        List<Veiculo> veiculos = new ArrayList<>();

        for (Dados ano : anosAvaliacao) {
            String enderecoAno = endereco + "/" + ano.codigo();
            json = consumoApi.obterDados(enderecoAno);
            Veiculo veiculo = conversor.obterDados(json, Veiculo.class);
            veiculos.add(veiculo);
        }
        return veiculos;
    }

    private void exibirVeiculos(List<Veiculo> veiculos) {
        System.out.println("\nTodos os veículos filtrados com a avaliação por ano: ");
        veiculos.forEach(System.out::println);
    }
}
