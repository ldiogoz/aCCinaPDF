# aCCinaPDF
Software de criação e validação de assinaturas digitais em ficheiros PDF com o Cartão de Cidadão.

Desenvolvido por:
* Luís Diogo Zambujo
* Micael Sousa Farinha

Orientado por:
* Prof. Miguel Frade
 
Trabalho desenvolvido no âmbito da unidade curricular "Projeto Informático" de Engenharia Informática no [Instituto Politécnico de Leiria](http://www.ipleiria.pt).


## Funcionalidades
O aCCinaPDF apresenta as seguintes funcionalidades e características:
* suporte para ficheiros PDF versão 1.7, para cumprir o RNID (Regulamento Nacional de Interoperabilidade Digital) -- obrigatório para toda a administração pública;
* não destrói a acessibilidade dos documentos, por isso permite cumprir as diretrizes WCAG 2.0 -- obrigatório para toda a administração pública;
* produz assinaturas digitais com validação de longo termo, sem esta opção as assinaturas digitais deixam de ser válidas quando o CC do assinante expirar, no máximo ao fim de 5 anos;
* permite fazer assinaturas digitais em lote;
* permite validação de assinaturas digitais em lote;
* multi sistema operativo (Windows e Linux, também deve funcionar no Mac OS, mas não foram realizados testes neste SO);
* facilidade de validação das assinaturas digitais por outros leitores de PDF graças à inclusão da hierarquia de certificados digitais do CC no próprio documento;
* fácil de instalar (não requer configurações da hierarquia de certificados digitais);
* é gratuito e open source (ver secção Licença);


## Requisitos do aCCinaPDF
Requisitos para **validar** PDFs assinados com o CC:
* instalar o Oracle Java, versão 1.7 ou superior (o aCCinaPDF foi desenvolvido em Java);
* ligação à Internet, sem a qual não é possível **assinar** ou **validar assinaturas** com validação de longo termo;

<br>
Requisitos para **assinar** PDFs com o CC:
* todos os requisitos enumerados acima;
* instalar o [software oficial do CC](https://www.cartaodecidadao.pt/), este software é necessário para o aCCinaPDF interagir com o CC;
* leitor de smartcards e respetivos *drivers* configurados;
* Cartão de Cidadão com assinatura digital ativa (se não estiver ativa deverá dirigir-se ao registo civil para pedir a ativação, terá de levar a carta que lhe foi enviada com os PINs);
* PIN da assinatura digital do CC (se errar o PIN 3 vezes o cartão fica bloqueado);


## Instalação
Descarregar a última versão do aCCinaPDF disponível [aqui](https://github.com/ldiogoz/aCCinaPDF/releases), descompactar e executar o ficheiro ```aCCinaPDF.exe```.

### Problemas conhecidos
* alguns anti-vírus podem impedir a execução do ficheiro ```aCCinaPDF.exe```, nesse caso fazer uma de duas coisas:
1. fazer duplo clique no ficheiro ```aCCinaPDF.jar```, ou
2. escrever na linha de comandos (dentro da diretoria do aCCinaPDF): ```java -jar aCCinaPDF.jar```

 
## Licença
O aCCinaPDF é disponibilizado sob a [licença AGPL](http://www.gnu.org/licenses/agpl.html).


    Copyright 2015 Luís Diogo Zambujo, Micael Sousa Farinha and Miguel Frade

    aCCinaPDF is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    aCCinaPDF is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with aCCinaPDF.  If not, see <http://www.gnu.org/licenses/>.
