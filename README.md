# Simulador de Sistema de Arquivos

## Resumo

Este projeto implementa um simulador de sistema de arquivos em Java chamado **FileSystemSimulator**. O simulador funciona em memória, permite manipular arquivos e diretórios por meio de um shell interativo e persiste o estado em disco usando serialização.

O sistema também possui journaling. Cada operação modificadora registra no arquivo `journal.log` o início da operação e seu resultado, indicando `COMMIT` em caso de sucesso ou `ROLLBACK` em caso de erro.

## Introdução

Sistemas de arquivos são componentes fundamentais dos sistemas operacionais. Eles organizam dados em arquivos e diretórios, permitindo armazenar, localizar, modificar, copiar, renomear e remover informações.

Em um sistema real, essas operações precisam ser confiáveis. Falhas de energia, travamentos ou erros durante uma escrita podem deixar dados inconsistentes. Por isso, muitos sistemas de arquivos utilizam mecanismos de recuperação, como journaling.

## Objetivo

O objetivo deste projeto é desenvolver um simulador acadêmico de sistema de arquivos em Java, com suporte a:

- arquivos e diretórios em memória;
- shell interativo no terminal;
- operações básicas sobre arquivos e diretórios;
- persistência em `filesystem.dat`;
- registro de operações em `journal.log`.

## Parte 1: Introdução ao Sistema de Arquivos com Journaling

### O que é um sistema de arquivos

Um sistema de arquivos é uma forma organizada de armazenar e recuperar dados. Ele define como arquivos são nomeados, como diretórios são estruturados e como as informações são acessadas.

Neste simulador, a raiz do sistema é representada por `/`. A partir dela, podem existir diretórios, arquivos e subdiretórios.

### Importância

A importância de um sistema de arquivos está em permitir que dados sejam armazenados de forma estruturada. Sem ele, seria difícil localizar informações, controlar nomes, separar arquivos por diretórios e manter uma organização lógica.

Em sistemas operacionais reais, o sistema de arquivos também precisa lidar com permissões, blocos em disco, metadados, falhas e recuperação.

### Journaling

Journaling é uma técnica usada para registrar operações antes ou durante sua execução. O objetivo é manter um histórico que ajude a identificar se uma operação começou, terminou corretamente ou falhou.

Neste projeto, o arquivo `journal.log` registra:

- timestamp;
- `BEGIN` da operação;
- `COMMIT` quando a operação termina com sucesso;
- `ROLLBACK` quando ocorre erro.

Exemplo:

```text
2026-06-04 20:00:00 | BEGIN | MKDIR | /documentos
2026-06-04 20:00:00 | COMMIT | MKDIR | /documentos
```

### Write-ahead logging e log-structured journaling

O conceito de **write-ahead logging** significa que uma operação deve ser registrada no log antes de ser aplicada definitivamente. Assim, caso ocorra falha, o sistema tem informações suficientes para saber o que estava acontecendo.

O **log-structured journaling** organiza alterações em forma de log, registrando as mudanças sequencialmente. Essa abordagem facilita auditoria e recuperação, pois o histórico das operações fica armazenado em ordem.

Este simulador usa uma versão simplificada dessas ideias, registrando o início e o resultado de cada operação.

## Parte 2: Arquitetura do Simulador

O projeto possui a seguinte estrutura:

```text
src/
├── Main.java
├── FileSystemSimulator.java
├── FSFile.java
├── Directory.java
└── Journal.java
```

### FSFile

A classe `FSFile` representa um arquivo do sistema. Ela possui:

- nome do arquivo;
- conteúdo textual;
- método para copiar o arquivo com outro nome.

### Directory

A classe `Directory` representa um diretório. Ela armazena:

- nome do diretório;
- mapa de subdiretórios;
- mapa de arquivos.

Ela também possui métodos para adicionar, remover, buscar e listar arquivos e diretórios.

### FileSystemSimulator

A classe `FileSystemSimulator` contém a lógica principal do sistema. Ela controla a raiz `/`, executa as operações obrigatórias e faz a persistência do estado em `filesystem.dat`.

Também é responsável por validar caminhos, localizar diretórios, localizar arquivos e impedir operações inválidas, como remover diretórios não vazios.

### Journal

A classe `Journal` registra as operações no arquivo `journal.log`. Para cada operação modificadora, são gravadas linhas com `BEGIN` e depois `COMMIT` ou `ROLLBACK`.

### Estrutura do log

Cada linha do journal segue o formato:

```text
timestamp | status | operacao | detalhe
```

Exemplo:

```text
2026-06-04 20:00:00 | BEGIN | CREATE | /documentos/aula.txt
2026-06-04 20:00:00 | COMMIT | CREATE | /documentos/aula.txt
```

## Parte 3: Implementação em Java

### Principais métodos

Na classe `FileSystemSimulator`, os principais métodos são:

- `createFile`: cria arquivo com conteúdo;
- `copyFile`: copia o conteúdo de um arquivo para outro;
- `deleteFile`: apaga arquivo;
- `rename`: renomeia arquivo ou diretório;
- `makeDirectory`: cria diretório;
- `removeDirectory`: apaga diretório vazio;
- `list`: lista arquivos e diretórios de um caminho;
- `tree`: mostra toda a estrutura a partir da raiz;
- `saveState`: salva o estado em `filesystem.dat`;
- `loadState`: carrega o estado salvo ao iniciar.

### Tabela de comandos

| Comando | Descrição | Exemplo |
| --- | --- | --- |
| `mkdir` | Cria um diretório | `mkdir /documentos` |
| `create` | Cria um arquivo com conteúdo | `create /documentos/aula.txt "Conteudo do arquivo"` |
| `copy` | Copia um arquivo | `copy /documentos/aula.txt /documentos/copia.txt` |
| `rename` | Renomeia arquivo ou diretório | `rename /documentos/copia.txt resumo.txt` |
| `ls` | Lista um diretório | `ls /documentos` |
| `delete` | Apaga um arquivo | `delete /documentos/aula.txt` |
| `rmdir` | Apaga diretório vazio | `rmdir /documentos` |
| `tree` | Mostra toda a árvore do sistema | `tree` |
| `help` | Mostra os comandos disponíveis | `help` |
| `exit` | Encerra o shell | `exit` |

## Parte 4: Instalação e funcionamento

### Compilar

No terminal, dentro da pasta do projeto, execute:

```bash
javac -d out src/*.java
```

### Executar

Depois de compilar, execute:

```bash
java -cp out Main
```

### Exemplos de uso

```text
mkdir /documentos
create /documentos/aula.txt "Conteudo do arquivo"
copy /documentos/aula.txt /documentos/copia.txt
rename /documentos/copia.txt resumo.txt
ls /documentos
delete /documentos/aula.txt
rmdir /documentos
tree
help
exit
```

Observação: o comando `rmdir /documentos` só funciona se o diretório estiver vazio. Caso existam arquivos ou subdiretórios, o simulador exibirá erro.

## Resultados esperados

Ao executar o projeto, espera-se que o usuário consiga criar uma estrutura de diretórios e arquivos em memória usando o shell interativo.

As operações bem-sucedidas devem ser salvas automaticamente no arquivo `filesystem.dat`. Assim, ao fechar e abrir o programa novamente, a estrutura criada anteriormente será carregada.

Também é esperado que o arquivo `journal.log` registre todas as operações modificadoras, indicando quando cada uma começou e se terminou com `COMMIT` ou `ROLLBACK`.
