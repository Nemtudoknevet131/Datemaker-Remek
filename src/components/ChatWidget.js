import React, { useState, useRef, useEffect } from 'react';
import '../styles/ChatWidget.css';

const GROQ_API_KEY = process.env.REACT_APP_GROQ_API_KEY;
const GROQ_API_URL = 'https://api.groq.com/openai/v1/chat/completions';

const CHAT_IMPORTS_KEY = 'datemakerChatImports';

const ChatWidget = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      type: 'bot',
      content: "Built to care, programmed to love",
      timestamp: new Date(),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [pendingSave, setPendingSave] = useState(null);
  const [chatTheme, setChatTheme] = useState('');
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const extractAIContent = (data) => {
    if (!data) return null;
    if (typeof data === 'string') return data;
    if (typeof data.message === 'string') return data.message;
    if (typeof data.content === 'string') return data.content;
    if (typeof data.reply === 'string') return data.reply;
    if (typeof data.text === 'string') return data.text;
    if (typeof data.error?.message === 'string') return data.error.message;
    if (Array.isArray(data.choices) && data.choices[0]?.message?.content) {
      return data.choices[0].message.content;
    }
    if (Array.isArray(data.choices) && typeof data.choices[0]?.text === 'string') {
      return data.choices[0].text;
    }
    return null;
  };

  const getFallbackResponse = (userMessage) => {
    const lower = userMessage.toLowerCase();

    if (lower.includes('date idea') || lower.includes('romantic')) {
      return "Try a rooftop dinner, sunset picnic, or a cozy movie night in. Want something low budget or adventurous?";
    }

    if (lower.includes('conversation') || lower.includes('talk')) {
      return "Ask about their favorite weekend ritual, a place they want to visit, or the best meal they've had.";
    }

    if (lower.includes('gift')) {
      return "Consider an experience gift, a small personalized item, or something tied to their hobby.";
    }

    if (lower.includes('nervous') || lower.includes('anxious')) {
      return "Totally normal. Arrive a bit early, take slow breaths, and focus on having fun.";
    }

    return "Tell me a bit more about your vibe, budget, and time window, and I'll suggest a plan.";
  };

  const shouldOfferSave = (content) => {
    if (!content) return false;
    return !/\?\s*$/.test(content.trim());
  };


  const getThemeTitle = (content) => {
    const firstLine = content
      .split('\n')
      .map((line) => line.trim())
      .find((line) => line.length > 0);
    const cleaned = (firstLine || 'Chat Date Idea').replace(/^[\d\-*\u2022\s]+/, '').trim();
    return cleaned.length > 60 ? `${cleaned.slice(0, 57)}...` : cleaned;
  };

  const saveChatIdea = (content) => {
    const stored = JSON.parse(localStorage.getItem(CHAT_IMPORTS_KEY) || '[]');
    const newIdea = {
      id: Date.now(),
      title: chatTheme ? getThemeTitle(chatTheme) : getThemeTitle(content),
      details: content,
      createdAt: new Date().toISOString(),
    };
    const updated = [newIdea, ...stored];
    localStorage.setItem(CHAT_IMPORTS_KEY, JSON.stringify(updated));
  };

  const handleSendMessage = async () => {
    if (!inputValue.trim()) return;

    const messageText = inputValue;
    if (!chatTheme) {
      setChatTheme(messageText);
    }
    setPendingSave(null);

    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: messageText,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue('');
    setIsTyping(true);

    try {
      if (!GROQ_API_KEY) {
        throw new Error('Missing GROQ API key');
      }

      const conversation = [...messages, userMessage]
        .filter((msg) => typeof msg?.content === 'string')
        .slice(-10)
        .map((msg) => ({
          role: msg.type === 'user' ? 'user' : 'assistant',
          content: msg.content,
        }));

      const response = await fetch(GROQ_API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${GROQ_API_KEY}`,
        },
        body: JSON.stringify({
          model: 'llama-3.1-8b-instant',
          messages: [
            {
              role: 'system',
              content: 'You are a friendly dating assistant. Keep replies concise and helpful.',
            },
            ...conversation,
          ],
          temperature: 0.7,
          max_tokens: 200,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        const errorMessage = extractAIContent(errorData) || `Request failed (${response.status})`;
        throw new Error(errorMessage);
      }

      const data = await response.json();
      const content = extractAIContent(data) || getFallbackResponse(messageText);
      const botResponse = {
        id: Date.now() + 1,
        type: 'bot',
        content,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, botResponse]);
      if (shouldOfferSave(content)) {
        setPendingSave({ id: botResponse.id, content });
      }
    } catch (error) {
      const botResponse = {
        id: Date.now() + 1,
        type: 'bot',
        content: `AI error: ${error.message || 'Request failed'}`,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, botResponse]);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const quickActions = [
    "Romantic dinner 🕯️",
    "Adventure date 🎢",
    "Cozy night in 🏠",
  ];
  return (
    <div className="chat-widget">
      <div className={`chat-popup ${isOpen ? 'open' : ''}`}>
        <div className="chat-popup-header">
          <button className="back-btn" onClick={() => setIsOpen(false)}>
            ←
          </button>
          <div className="chat-popup-header-info">
            <div className="bot-avatar-widget">
              <span>🤖</span>
            </div>
            <div className="bot-details">
              <h3>Date Crafter</h3>
              <span className="bot-subtitle">Rizz machine</span>
            </div>
          </div>
          <button className="more-btn">⋮</button>
        </div>

        <div className="chat-popup-messages">
          {messages.map((message) => (
            <div key={message.id} className={`popup-message ${message.type}`}>
              {message.type === 'bot' && (
                <div className="popup-message-header">
                  <span className="popup-sender">Datemaker Bot</span>
                  <span className="popup-time">
                    {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} 
                  </span>
                </div>
              )}
              <div className="popup-message-bubble">
                <p style={{ whiteSpace: 'pre-wrap' }}>{message.content}</p>
              </div>
            </div>
          ))}
          
          {isTyping && (
            <div className="popup-message bot">
              <div className="popup-message-header">
                <span className="popup-sender">Datemaker Bot</span>
              </div>
              <div className="popup-message-bubble typing">
                <div className="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          )}
          
          <div ref={messagesEndRef} />
        </div>

        {messages.length <= 2 && (
          <div className="chat-popup-actions">
            {quickActions.map((action, index) => (
              <button
                key={index}
                className="quick-action-btn"
                onClick={() => {
                  setInputValue(action.replace(/[^\w\s]/g, '').trim());
                }}
              >
                {action}
              </button>
            ))}
          </div>
        )}

        {pendingSave && !isTyping && (
          <div className="chat-save-actions" role="group" aria-label="Save date idea">
            <button
              className="save-choice-btn save"
              onClick={() => {
                saveChatIdea(pendingSave.content);
                setPendingSave(null);
              }}
              aria-label="Save date idea"
            >
              <span className="save-icon">❤</span>
              <span>Save this date</span>
            </button>
            <button
              className="save-choice-btn dismiss"
              onClick={() => setPendingSave(null)}
              aria-label="Dismiss save prompt"
            >
              ✕
            </button>
          </div>
        )}

        <div className="chat-popup-input">
          <button className="attach-btn">📎</button>
          <input
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Write your message..."
          />
          <button className="emoji-btn">😊</button>
          <button 
            className="voice-btn"
            onClick={handleSendMessage}
            disabled={!inputValue.trim()}
          >
            {inputValue.trim() ? '➤' : '🎤'}
          </button>
        </div>

        <div className="chat-popup-footer">
          Powered by <span className="footer-brand">Datemaker💕</span>
        </div>
      </div>

      <button 
        className={`chat-fab ${isOpen ? 'active' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Open chat"
      >
        {isOpen ? (
          <span className="fab-icon close">✕</span>
        ) : (
          <svg className="fab-icon sparkle" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="sparkleGradient" x1="50%" y1="0%" x2="50%" y2="100%">
                <stop offset="0%" stopColor="#a78bfa" />
                <stop offset="50%" stopColor="#3b82f6" />
                <stop offset="100%" stopColor="#0ea5e9" />
              </linearGradient>
            </defs>
            <path 
              d="M50 0 C52 35, 65 48, 100 50 C65 52, 52 65, 50 100 C48 65, 35 52, 0 50 C35 48, 48 35, 50 0Z" 
              fill="url(#sparkleGradient)"
            />
          </svg>
        )}
      </button>
    </div>
  );
};

export default ChatWidget;
