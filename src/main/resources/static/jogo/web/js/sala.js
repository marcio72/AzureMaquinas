    const socket = new SockJS('/ws-jogo');
	const stomp = Stomp.over(socket);
	stomp.debug = null;
	let jaMostrouVitoria = false;

	stomp.connect({}, () => {
       console.log("✅ Conectado!");

       // 1. Canal do JOGO (Mesa e Cartas)
       stomp.subscribe(`/topic/partida/${PARTIDA_ID}`, msg => {
           const data = JSON.parse(msg.body);
           atualizarTela(data);
       });

       // 2. Canal de CHAT PÚBLICO (Geral)
       stomp.subscribe(`/topic/chat/${PARTIDA_ID}`, msg => {
           const chatData = JSON.parse(msg.body);
           receberMensagem(chatData);
       });

       // 3. Canal de CHAT PRIVADO (NOVO - Só eu escuto)
       stomp.subscribe(`/topic/chat/${PARTIDA_ID}/privado/${NOME_JOGADOR}`, msg => {
           const chatData = JSON.parse(msg.body);
           receberMensagem(chatData);
       });

       verificarStatusInicial();
    });

	function verificarStatusInicial() {
	   const cartasMao = document.getElementById("minhas-cartas").children.length;
	   if (cartasMao === 0) {
		document.getElementById("btn-iniciar").style.display = "block";
	   }
	}

	function iniciarPartida() {
	   fetch(`/api/partidas/${PARTIDA_ID}/iniciar`, { method: 'POST' })
	   .then(res => {
		if(res.ok) document.getElementById("btn-iniciar").style.display = "none";
	   });
	}

	function atualizarTela(data) {

	// --- CONTROLE DE VISIBILIDADE DO CHAT ---
       const btnChat = document.getElementById('botaoChat');
       const janelaChat = document.getElementById('janelaChat');

       // Se o jogo está ROLANDO ou ACABOU, libera o chat
       if (data.status === 'EM_ANDAMENTO' || data.status === 'FINALIZADA') {
           btnChat.style.display = 'flex'; // Mostra o botão
       }
       // Se ainda está AGUARDANDO, esconde tudo
       else {
           btnChat.style.display = 'none'; // Esconde o botão
           janelaChat.classList.remove('ativo'); // Fecha a janela se estiver aberta
       }

	   if (data.status === 'FINALIZADA') {
               // Se ainda NÃO mostrei a vitória, mostro agora e travo.
               if (!jaMostrouVitoria) {
                   mostrarVitoria(data.jogadores);
                   jaMostrouVitoria = true; // Trava para não rodar de novo
               }
               return; // Para a função aqui, ignorando quem sai ou entra depois
           }

	   const jogadorDaVez = data.jogadorDaVez;
	   const eMinhaVez = (jogadorDaVez === NOME_JOGADOR);

	   const divJogadores = document.getElementById("lista-jogadores");
	   if (data.jogadores) {
		 divJogadores.innerHTML = "";
		 data.jogadores.forEach(j => {
		   const div = document.createElement("div");
		   const isVezDesse = (j.nome === jogadorDaVez);
		   div.className = `player-chip ${isVezDesse ? 'vez-atual' : ''}`;
		   const icon = isVezDesse ? '<i class="fas fa-dharmachakra fa-spin"></i>' : '<i class="fas fa-user"></i>';
		   div.innerHTML = `${icon} ${j.nome} &nbsp;<b style="color: ${isVezDesse ? '#000' : '#dcb95a'}">${j.pontos}</b>`;
		   divJogadores.appendChild(div);
		 });
	   }

	   if (data.status === 'EM_ANDAMENTO') {
		 document.getElementById("btn-iniciar").style.display = "none";
	   }

	   const boxNaipe = document.getElementById("box-naipe");
	   const naipeImg = document.getElementById("naipe-img");
	   if (data.naipeDaRodada) {
		 let n = data.naipeDaRodada.toLowerCase();
		 if (n !== 'paus' && n.endsWith('s')) n = n.slice(0, -1);
		 naipeImg.src = `/jogo/img/cartas/${n}/a_${n}.png`;
		 boxNaipe.style.display = "flex";
	   } else {
		 boxNaipe.style.display = "none";
	   }

	   const mesa = document.getElementById("mesa");
	   mesa.innerHTML = "";
	   if (data.mesa && data.mesa.length > 0) {
		 const totalCartas = data.mesa.length;
		 const isMobile = window.innerWidth < 500;
		 const spreadFactor = isMobile ? 15 : 30;

		 data.mesa.forEach((jogada, index) => {
		   const img = document.createElement("img");
		   img.className = "mesa-carta";
		   let n = String(jogada.naipe).toLowerCase();
		   let v = String(jogada.valor).toLowerCase();
		   img.src = `/jogo/img/cartas/${n}/${v}_${n}.png`;
		   const meio = (totalCartas - 1) / 2;
		   const distanciaDoMeio = index - meio;
		   const offsetX = distanciaDoMeio * spreadFactor;
		   const rot = distanciaDoMeio * (isMobile ? 5 : 8);
		   const z = 10 + index;
		   img.style.transform = `translate(-50%, -50%) translateX(${offsetX}px) rotate(${rot}deg)`;
		   img.style.zIndex = z;
		   mesa.appendChild(img);
		 });
	   }

	   const minhasCartasDiv = document.getElementById("minhas-cartas");
	   const statusArea = document.querySelector(".status-text");

	   if (eMinhaVez) {
		 statusArea.innerHTML = `<i class="fas fa-exclamation-circle" style="color: #dcb95a"></i> SUA VEZ DE JOGAR!`;
		 statusArea.style.border = "1px solid #dcb95a";
		 statusArea.style.color = "#dcb95a";
		 minhasCartasDiv.classList.remove("bloqueado");
	   } else {
		 statusArea.innerHTML = `Aguardando ${jogadorDaVez}...`;
		 statusArea.style.border = "1px solid #333";
		 statusArea.style.color = "#888";
		 minhasCartasDiv.classList.add("bloqueado");
	   }

	   const eu = data.jogadores.find(j => j.nome === NOME_JOGADOR);
	   if (eu && eu.cartas) {
		 minhasCartasDiv.innerHTML = "";
		 eu.cartas.forEach(c => {
		   const img = document.createElement("img");
		   img.className = "carta-img";
		   let n = String(c.naipe).toLowerCase();
		   let v = String(c.valor).toLowerCase();
		   img.src = `/jogo/img/cartas/${n}/${v}_${n}.png`;
		   img.dataset.naipe = c.naipe;
		   img.dataset.valor = c.valor;
		   img.onclick = () => jogarComAnimacao(img);
		   minhasCartasDiv.appendChild(img);
		 });
	   }

	   const select = document.getElementById('chatDestino');
       if (select) { // Proteção para não dar erro se o html não carregar
           const valorAtual = select.value;

           // Limpa tudo e recria a opção TODOS
           select.innerHTML = '<option value="TODOS">Todos</option>';

           if (data.jogadores) {
               data.jogadores.forEach(j => {
                   // Adiciona todos MENOS eu mesmo
                   if (j.nome !== NOME_JOGADOR) {
                       const opt = document.createElement('option');
                       opt.value = j.nome;
                       opt.innerText = j.nome;
                       select.appendChild(opt);
                   }
               });
           }

           // Se eu tinha selecionado alguém que ainda está na sala, mantém selecionado
           if ([...select.options].some(o => o.value === valorAtual)) {
               select.value = valorAtual;
           }
       }
	}

	function jogar(img) {
	   stomp.send(`/app/partida/${PARTIDA_ID}/jogar`, {}, JSON.stringify({
		 nomeJogador: NOME_JOGADOR,
		 carta: { naipe: img.dataset.naipe, valor: img.dataset.valor }
	   }));
	}

	function jogarComAnimacao(imgOriginal) {
	   const audio = document.getElementById("som-carta");
	   if(audio) { audio.currentTime = 0; audio.play().catch(()=>{}); }
	   animarVoo(imgOriginal);
	   jogar(imgOriginal);
	}

	function animarVoo(elementoOriginal) {
	   const rectOrigem = elementoOriginal.getBoundingClientRect();
	   const rectMesa = document.getElementById("mesa").getBoundingClientRect();

	   const clone = elementoOriginal.cloneNode(true);
	   clone.style.width = elementoOriginal.offsetWidth + "px";
	   clone.style.height = elementoOriginal.offsetHeight + "px";
	   clone.style.position = "fixed";
	   clone.style.left = rectOrigem.left + "px";
	   clone.style.top = rectOrigem.top + "px";
	   clone.classList.add("carta-voando");
	   document.body.appendChild(clone);

	   elementoOriginal.style.opacity = "0";

	   requestAnimationFrame(() => {
		 const destinoX = rectMesa.left + (rectMesa.width / 2) - (rectOrigem.width / 2);
		 const destinoY = rectMesa.top + (rectMesa.height / 2) - (rectOrigem.height / 2);
		 clone.style.left = destinoX + "px";
		 clone.style.top = destinoY + "px";
		 clone.style.transform = "rotate(180deg) scale(0.9)";
	   });
	   setTimeout(() => clone.remove(), 700);
	}

	function sairDoJogo() {
	   fetch(`/api/partidas/${PARTIDA_ID}/sair?nome=${NOME_JOGADOR}`, { method: 'POST' })
	   .then(() => window.location.href = "/jogo")
	   .catch(() => window.location.href = "/jogo");
	}

	function mostrarVitoria(jogadores) {
        const overlay = document.getElementById("tela-vitoria");
        const divVencedor = document.getElementById("vencedor-nome");
        const listaPodio = document.getElementById("lista-podio");

        // 1. Descobre quem ganhou
        const ranking = jogadores.sort((a, b) => b.pontos - a.pontos);
        const campeao = ranking[0];

        // 2. Preenche o HTML do Placar
        divVencedor.innerText = campeao.nome;
        listaPodio.innerHTML = "";

        ranking.forEach((j, index) => {
            const item = document.createElement("div");
            item.className = "podio-item";
            let icone = index === 0 ? "👑" : `#${index + 1}`;
            let cor = index === 0 ? "var(--accent)" : "#fff";
            item.innerHTML = `<span style="color:${cor}">${icone} ${j.nome}</span> <span>${j.pontos} pts</span>`;
            listaPodio.appendChild(item);
        });

        // 3. Mostra a tela preta para TODOS
        overlay.style.display = "flex";

        // 4. A FESTA É SÓ PARA O VENCEDOR! (A MUDANÇA ESTÁ AQUI)
        if (campeao.nome === NOME_JOGADOR) {
            iniciarShowDaVitoria(); // Chuva de emojis e confetes

            // (Opcional) Tocar som de vitória aqui se você tiver
            // const audioWin = new Audio('/jogo/sounds/win.mp3');
            // audioWin.play();
        }
    }

	function iniciarShowDaVitoria() {
	   chuvaDeEmojisLenta();
	   dispararConfetes();
	}

	function dispararConfetes() {
	   const end = Date.now() + (3 * 1000);
	   const zIndexConfete = 9999;
	   const colors = ['#dcb95a', '#ffffff', '#ff0000'];

	   (function frame() {
		  confetti({
			 particleCount: 5,
			 angle: 60,
			 spread: 55,
			 origin: { x: 0 },
			 zIndex: zIndexConfete,
			 colors: colors
		  });
		  confetti({
			 particleCount: 5,
			 angle: 120,
			 spread: 55,
			 origin: { x: 1 },
			 zIndex: zIndexConfete,
			 colors: colors
		  });

		  if (Date.now() < end) {
			 requestAnimationFrame(frame);
		  }
	   }());
	}

	function chuvaDeEmojisLenta() {
	   const container = document.body;
	   const emojis = ["🏆", "🃏", "✨", "💰", "👑"];

	   for(let i=0; i<30; i++) {
		  const el = document.createElement("div");
		  el.innerText = emojis[Math.floor(Math.random() * emojis.length)];
		  el.style.position = "fixed";
		  el.style.left = Math.random() * 100 + "vw";
		  el.style.top = "-10vh";
		  el.style.fontSize = (Math.random() * 30 + 30) + "px";
		  el.style.opacity = Math.random() + 0.5;
		  el.style.zIndex = "10000";

		  const duracao = Math.random() * 3 + 4;
		  el.style.transition = `top ${duracao}s linear, transform ${duracao}s ease-in-out`;

		  container.appendChild(el);

		  requestAnimationFrame(() => {
			 setTimeout(() => {
				el.style.top = "110vh";
				el.style.transform = `rotate(${Math.random() * 360}deg)`;
			 }, Math.random() * 2000);
		  });

		  setTimeout(() => el.remove(), duracao * 1000 + 3000);
	   }
	}

	// --- FUNÇÕES DO CHAT ---
    function toggleChat() {
        document.getElementById('janelaChat').classList.toggle('ativo');
    }

    function handleEnter(e) {
        if(e.key === 'Enter') enviarMensagem();
    }

    function enviarMensagem() {
        const input = document.getElementById('chatInput');
        const selectDestino = document.getElementById('chatDestino');

        // Pega o valor do select (ou 'TODOS' se der erro)
        const destino = selectDestino ? selectDestino.value : "TODOS";
        const texto = input.value.trim();

        if(!texto) return;

        stomp.send(`/app/chat/${PARTIDA_ID}/enviar`, {}, JSON.stringify({
            remetente: NOME_JOGADOR,
            conteudo: texto,
            destinatario: destino // <--- AQUI VAI O DESTINO (Nome ou TODOS)
        }));

        input.value = "";
        input.focus();
    }

    function receberMensagem(msg) {
        const chatBody = document.getElementById('chatBody');
        const janela = document.getElementById('janelaChat');
        const isMe = (msg.remetente === NOME_JOGADOR);

        // 1. Cria o HTML da mensagem (igual antes)
        const div = document.createElement('div');
        div.className = `msg ${isMe ? 'eu' : 'outro'}`;

        // Se for privada, muda a cor ou coloca um aviso
        if (msg.tipo === 'PRIVADA') {
            div.style.border = "1px solid #dcb95a"; // Borda dourada para privada
            div.innerHTML = `<span class="msg-nome">[PRIVADO] ${msg.remetente}</span>${msg.conteudo}`;
        } else {
            let htmlNome = isMe ? '' : `<span class="msg-nome">${msg.remetente}</span>`;
            div.innerHTML = `${htmlNome}${msg.conteudo}`;
        }

        chatBody.appendChild(div);
        chatBody.scrollTop = chatBody.scrollHeight;

        // --- NOVO: NOTIFICAÇÃO ---
        // Se a janela NÃO tem a classe 'ativo' (está fechada), avisa o usuário
        if (!janela.classList.contains('ativo') && !isMe) {
            // Toca som
            const audio = document.getElementById('audio-chat');
            if(audio) audio.play().catch(e => console.log("Erro som", e));

            // Mostra bolinha
            document.getElementById('notificacaoMsg').style.display = 'block';
        }
    }

    // Função para abrir o chat e sumir com a notificação
    function abrirChatELimpar() {
        toggleChat(); // Abre a janela
        // Esconde a bolinha vermelha
        const notificacao = document.getElementById('notificacaoMsg');
        if (notificacao) notificacao.style.display = 'none';
    }
