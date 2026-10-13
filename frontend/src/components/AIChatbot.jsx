import React, { useState, useEffect, useRef } from "react";
import { aiAPI } from "../services/api";

function AIChatbot() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: "bot",
      text: "👋 Hello! I am your **PG Management AI Assistant**.\n\nAsk me anything about available rooms, rent, meal timings, amenities, your dues, or PG rules!",
      suggestions: [
        "Which rooms are available?",
        "What is the rent for sharing rooms?",
        "Today's meal timings",
        "What amenities are included?",
      ],
      aiPowered: true,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  const user = JSON.parse(localStorage.getItem("user") || "null");

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
      inputRef.current?.focus();
    }
  }, [isOpen, messages, loading]);

  const handleSend = async (messageText) => {
    const textToSend = (messageText || input).trim();
    if (!textToSend || loading) return;

    setError(null);
    setInput("");

    const userMessage = {
      id: Date.now(),
      sender: "user",
      text: textToSend,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    };

    setMessages((prev) => [...prev, userMessage]);
    setLoading(true);

    try {
      // Build conversation history for context
      const history = messages.slice(-6).map((m) => ({
        role: m.sender === "user" ? "user" : "model",
        text: m.text,
      }));

      const response = await aiAPI.chat({
        message: textToSend,
        userEmail: user?.email,
        userRole: user?.role,
        userId: user?.userId,
        history: history,
      });

      const botMessage = {
        id: Date.now() + 1,
        sender: "bot",
        text: response.data.reply,
        suggestions: response.data.suggestions || [],
        aiPowered: response.data.aiPowered,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      };

      setMessages((prev) => [...prev, botMessage]);
    } catch (err) {
      console.error("AI Chat error:", err);
      setError("AI service encountered a temporary error. Please retry.");
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: "bot",
          text: "⚠️ I am temporarily having trouble reaching the server. Please check that the Spring Boot backend is running or click below to retry.",
          suggestions: ["Which rooms are available?", "What amenities are included?"],
          isError: true,
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const clearChat = () => {
    setMessages([
      {
        id: Date.now(),
        sender: "bot",
        text: "Chat cleared. How can I help you today?",
        suggestions: ["Which rooms are available?", "What is the monthly rent?", "Check meal schedule"],
        aiPowered: true,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      },
    ]);
    setError(null);
  };

  // Render text with simple bolding and bullet formatting
  const formatText = (text) => {
    if (!text) return "";
    return text.split("\n").map((line, idx) => {
      // Bold rendering
      const parts = line.split(/(\*\*.*?\*\*)/g).map((part, i) => {
        if (part.startsWith("**") && part.endsWith("**")) {
          return <strong key={i}>{part.slice(2, -2)}</strong>;
        }
        return part;
      });

      if (line.startsWith("• ") || line.startsWith("- ")) {
        return (
          <div key={idx} className="ai-chat-bullet">
            {parts}
          </div>
        );
      }
      return (
        <p key={idx} className="ai-chat-line">
          {parts}
        </p>
      );
    });
  };

  return (
    <div className="ai-chatbot-wrapper">
      {/* Floating Toggle Button */}
      {!isOpen && (
        <button
          className="ai-chat-fab"
          onClick={() => setIsOpen(true)}
          title="Open PG AI Assistant"
          aria-label="Open PG AI Assistant"
        >
          <span className="ai-fab-icon">🤖</span>
          <span className="ai-fab-text">AI Assistant</span>
          <span className="ai-fab-pulse"></span>
        </button>
      )}

      {/* Chat Window */}
      {isOpen && (
        <div className="ai-chat-window">
          {/* Header */}
          <div className="ai-chat-header">
            <div className="ai-chat-header-info">
              <div className="ai-chat-avatar">🤖</div>
              <div>
                <h3>PG AI Assistant</h3>
                <span className="ai-chat-status">
                  <span className="ai-status-dot"></span>
                  {user ? `${user.role} Mode` : "Guest"} • Online
                </span>
              </div>
            </div>
            <div className="ai-chat-header-actions">
              <button
                className="ai-icon-btn"
                onClick={clearChat}
                title="Clear conversation"
              >
                🗑️
              </button>
              <button
                className="ai-icon-btn"
                onClick={() => setIsOpen(false)}
                title="Close Chat"
              >
                ✕
              </button>
            </div>
          </div>

          {/* Messages Container */}
          <div className="ai-chat-messages">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`ai-message-row ${
                  m.sender === "user" ? "user-row" : "bot-row"
                }`}
              >
                {m.sender === "bot" && <div className="ai-bot-avatar">🤖</div>}
                <div
                  className={`ai-message-bubble ${
                    m.sender === "user" ? "user-bubble" : "bot-bubble"
                  } ${m.isError ? "error-bubble" : ""}`}
                >
                  <div className="ai-bubble-content">{formatText(m.text)}</div>
                  <div className="ai-bubble-footer">
                    <span>{m.timestamp}</span>
                    {m.sender === "bot" && (
                      <span className="ai-badge-indicator">
                        {m.aiPowered ? "✨ AI Generated" : "🛡️ Grounded Data"}
                      </span>
                    )}
                  </div>

                  {/* Suggestion Chips */}
                  {m.suggestions && m.suggestions.length > 0 && (
                    <div className="ai-suggestions-container">
                      {m.suggestions.map((s, idx) => (
                        <button
                          key={idx}
                          className="ai-suggestion-chip"
                          onClick={() => handleSend(s)}
                          disabled={loading}
                        >
                          {s}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}

            {/* Loading Indicator */}
            {loading && (
              <div className="ai-message-row bot-row">
                <div className="ai-bot-avatar">🤖</div>
                <div className="ai-message-bubble bot-bubble loading-bubble">
                  <div className="ai-typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <span className="ai-typing-text">AI is searching PG records...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Error Banner */}
          {error && (
            <div className="ai-chat-error-bar">
              <span>{error}</span>
              <button onClick={() => handleSend(messages[messages.length - 1]?.text)}>
                Retry
              </button>
            </div>
          )}

          {/* Input Box */}
          <form
            className="ai-chat-input-form"
            onSubmit={(e) => {
              e.preventDefault();
              handleSend();
            }}
          >
            <input
              ref={inputRef}
              type="text"
              placeholder="Ask about rooms, rent, meals, or complaints..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={loading}
            />
            <button
              type="submit"
              disabled={!input.trim() || loading}
              className="ai-send-btn"
            >
              ➤
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default AIChatbot;
