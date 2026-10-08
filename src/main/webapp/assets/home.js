(() => {
    const chatLauncher = document.querySelector('#chat-launcher');
    const chatPanel = document.querySelector('#chat-panel');
    const chatClose = document.querySelector('#chat-close');
    const chatForm = document.querySelector('#chat-form');
    const chatInput = document.querySelector('#chat-input');
    const chatMessages = document.querySelector('#chat-messages');

    if (!chatLauncher || !chatPanel || !chatClose || !chatForm || !chatInput || !chatMessages) {
        return;
    }

    const setChatOpen = (open) => {
        chatPanel.hidden = !open;
        chatLauncher.setAttribute('aria-expanded', String(open));
        if (open) {
            chatInput.focus();
        } else {
            chatLauncher.focus();
        }
    };

    const appendMessage = (text, className, action) => {
        const message = document.createElement('div');
        message.className = `chat-message ${className}`;
        const content = document.createElement('span');
        content.textContent = text;
        message.append(content);

        if (action) {
            const link = document.createElement('a');
            link.className = 'chat-message-link';
            link.href = action.href;
            link.textContent = action.label;
            message.append(document.createElement('br'), link);
        }

        chatMessages.append(message);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    };

    const makeReply = (question) => {
        const normalizedQuestion = question.toLowerCase();
        if (/book|appointment|schedule|slot|visit/.test(normalizedQuestion)) {
            return {
                text: 'You can request a visit with a doctor and choose from their published availability.',
                action: { href: chatPanel.dataset.bookUrl, label: 'Find an appointment' }
            };
        }
        if (/sign|log.?in|account|password/.test(normalizedQuestion)) {
            return {
                text: 'Use your email and password to access your Carepoint account.',
                action: { href: chatPanel.dataset.loginUrl, label: 'Go to sign in' }
            };
        }
        if (/record|history|note/.test(normalizedQuestion)) {
            const needsSignIn = chatPanel.dataset.recordsUrl.endsWith('/login');
            return {
                text: 'Your medical records are available in your patient workspace after you sign in.',
                action: {
                    href: chatPanel.dataset.recordsUrl,
                    label: needsSignIn ? 'Sign in to continue' : 'Open medical records'
                }
            };
        }
        if (/doctor|clinician|clinic/.test(normalizedQuestion)) {
            return {
                text: 'Patients can browse doctors while booking. Doctors and clinic administrators can sign in to their workspaces.',
                action: { href: chatPanel.dataset.loginUrl, label: 'Open sign in' }
            };
        }
        return {
            text: 'I can help with appointments, signing in, or finding your records. I cannot provide medical advice. For health concerns, contact your care team.'
        };
    };

    const sendQuestion = (question) => {
        const trimmedQuestion = question.trim();
        if (!trimmedQuestion) {
            return;
        }
        appendMessage(trimmedQuestion, 'chat-user-message');
        const reply = makeReply(trimmedQuestion);
        appendMessage(reply.text, 'chat-bot-message', reply.action);
    };

    chatLauncher.addEventListener('click', () => setChatOpen(chatPanel.hidden));
    chatClose.addEventListener('click', () => setChatOpen(false));
    chatForm.addEventListener('submit', (event) => {
        event.preventDefault();
        sendQuestion(chatInput.value);
        chatInput.value = '';
        chatInput.focus();
    });
    chatPanel.querySelectorAll('[data-question]').forEach((button) => {
        button.addEventListener('click', () => sendQuestion(button.dataset.question));
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !chatPanel.hidden) {
            setChatOpen(false);
        }
    });
})();