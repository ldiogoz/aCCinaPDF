# aCCinaPDF
Software de criação e validação de assinaturas digitais em ficheiros PDF com o Cartão de Cidadão.

Desenvolvido por:
* Luís Diogo Zambujo
* Micael Sousa Farinha

Orientado por:
* Prof. Miguel Frade
 
Trabalho desenvolvido no âmbito da unidade curricular "Projeto Informático" de Engenharia Informática no [Instituto Politécnico de Leiria](http://www.ipleiria.pt).


## Enquadramento
Atualmente existem várias aplicações que permitem criar assinaturas digitais em ficheiros PDF, mas nenhuma suportava todos os requisitos:
* suporte para certificados digitais armazenados no Cartão de Cidadão (CC);
* suporte para ficheiros PDF versão 1.7, para cumprir o RNID (Regulamento Nacional de Interoperabilidade Digital) -- **obrigatório para toda a administração pública**;
* cumprir o nível mínimo “A” de conformidade na acessibilidade dos documentos de acordo com as diretrizes WCAG 2.0 (Web Content Accessibility Guidelines) -- **obrigatório para toda a administração pública**;
* produzir assinaturas digitais com validação de longo termo, sem esta opção as assinaturas digitais deixam de ser válidas quando o CC do assinante expirar (no máximo ao fim de 5 anos);
* multi sistema operativo (Windows, Linux e Mac OS)
* gratuito, ou de muito baixo custo;

Este projeto nasceu com o objetivo de cumprir todos os requisitos enumerados e assim fumentar a utilização do CC.

## Requisitos
Para realizar assinaturas digitais com o aCCinaPDF tem de cumprir os seguintes requisitos:
* instalar o [software oficial do CC](https://www.cartaodecidadao.pt/), este software é necessário para o aCCinaPDF interagir com o CC;
* instalar o Oracle Java, versão 1.7 ou superior (o aCCinaPDF foi desenvolvido em Java);
* leitor de smartcards e respetivos drivers configurados;
* Cartão de Cidadão com assinatura digital ativa (se não estiver ativa deverá dirigir-se ao registo civil para pedir a ativação, terá de levar a carta que lhe foi enviada com os PINs);
* PIN da assinatura digital do CC (se errar o PIN 3 vezes o cartão fica bloqueado);
* ligação à Internet, sem a qual não é possível produzir assinaturas com validação de longo termo;

## Instalação
Para instalar o aCCinaPDF existem duas formas:
  1. clonar o repositório git e compilar o código java, 
  ```bash
    ant jar
    ant run
  ```
  2. ou, descarregar a última versão já compilada [aqui](https://github.com/ldiogoz/aCCinaPDF/releases), descompactar e executar o ficheiro aCCinaPDF.jar



 
## Licença
    Copyright 2015 Luís Diogo Zambujo, Micael Sousa Farinha and Miguel Frade

    aCCinaPDF is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    aCCinaPDF is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with aCCinaPDF.  If not, see <http://www.gnu.org/licenses/>.
