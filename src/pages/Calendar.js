import React, { useEffect, useRef, useState } from 'react';
import { Autocomplete, useJsApiLoader } from '@react-google-maps/api';
import '../styles/Calendar.css';

const CHAT_IMPORTS_KEY = 'datemakerChatImports';
const FEED_SAVED_KEY = 'datemakerFeedSavedIdeas';
const libraries = ['places'];

const Calendar = () => {
  const { isLoaded } = useJsApiLoader({
    googleMapsApiKey: process.env.REACT_APP_GOOGLE_MAPS_API_KEY || '',
    libraries,
  });

  const planAutocompleteRef = useRef(null);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(null);
  const [chatImports, setChatImports] = useState([]);
  const [showChatImports, setShowChatImports] = useState(false);
  const [feedSavedIdeas, setFeedSavedIdeas] = useState([]);
  const [showPlanModal, setShowPlanModal] = useState(false);
  const [planForm, setPlanForm] = useState({
    title: '',
    location: '',
    time: '19:00',
    type: 'special',
    date: '',
  });

  const [plannedDates, setPlannedDates] = useState([
    {
      id: 1,
      date: new Date(2026, 0, 30),
      title: "Romantic Dinner",
      location: "La Bella Italia",
      time: "7:00 PM",
      type: "romantic",
    },
    {
      id: 2,
      date: new Date(2026, 1, 14),
      title: "Valentine's Day Special",
      location: "Rooftop Bar",
      time: "6:30 PM",
      type: "special",
    },
    {
      id: 3,
      date: new Date(2026, 1, 8),
      title: "Adventure Hike",
      location: "Mountain Trail",
      time: "9:00 AM",
      type: "adventure",
    },
    {
      id: 4,
      date: new Date(2026, 0, 28),
      title: "Movie Night In",
      location: "Home",
      time: "8:00 PM",
      type: "cozy",
    },
  ]);

  useEffect(() => {
    const stored = JSON.parse(localStorage.getItem(CHAT_IMPORTS_KEY) || '[]');
    setChatImports(stored);
  }, []);

  useEffect(() => {
    const stored = JSON.parse(localStorage.getItem(FEED_SAVED_KEY) || '[]');
    setFeedSavedIdeas(stored);
  }, []);

  const monthNames = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  ];

  const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  const getDaysInMonth = (date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDayOfMonth = new Date(year, month, 1).getDay();
    return { daysInMonth, firstDayOfMonth };
  };

  const { daysInMonth, firstDayOfMonth } = getDaysInMonth(currentDate);

  const prevMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    setSelectedDate(null);
  };

  const nextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    setSelectedDate(null);
  };

  const getDateForDay = (day) => {
    return new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
  };

  const formatDateInput = (date) => {
    const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 10);
  };

  const getDatesForDay = (day) => {
    const date = getDateForDay(day);
    return plannedDates.filter(d => 
      d.date.getDate() === date.getDate() &&
      d.date.getMonth() === date.getMonth() &&
      d.date.getFullYear() === date.getFullYear()
    );
  };

  const isToday = (day) => {
    const today = new Date();
    const date = getDateForDay(day);
    return date.toDateString() === today.toDateString();
  };

  const isSelected = (day) => {
    if (!selectedDate) return false;
    const date = getDateForDay(day);
    return date.toDateString() === selectedDate.toDateString();
  };

  const handleDayClick = (day) => {
    setSelectedDate(getDateForDay(day));
  };

  const openPlanModal = () => {
    const defaultDate = selectedDate || new Date();
    setPlanForm({
      title: '',
      location: '',
      time: '19:00',
      type: 'special',
      date: formatDateInput(defaultDate),
    });
    setShowPlanModal(true);
  };

  const handlePlanChange = (event) => {
    const { name, value } = event.target;
    setPlanForm((prev) => ({ ...prev, [name]: value }));
  };

  const handlePlanLocationLoad = (autocomplete) => {
    planAutocompleteRef.current = autocomplete;
  };

  const handlePlanPlaceChanged = () => {
    const autocomplete = planAutocompleteRef.current;
    if (!autocomplete) return;
    const place = autocomplete.getPlace();
    const name = place?.name || '';
    const address = place?.formatted_address || '';
    const finalPlace = name && address ? `${name}, ${address}` : name || address;
    if (finalPlace) {
      setPlanForm((prev) => ({ ...prev, location: finalPlace }));
    }
  };

  const handlePlanSubmit = (event) => {
    event.preventDefault();
    if (!planForm.date) return;
    const planDate = new Date(`${planForm.date}T00:00:00`);
    const newDate = {
      id: Date.now(),
      date: planDate,
      title: planForm.title || 'Planned Date',
      location: planForm.location || 'TBD',
      time: planForm.time || '7:00 PM',
      type: planForm.type,
    };

    setPlannedDates((prev) => [newDate, ...prev]);
    setSelectedDate(planDate);
    setShowPlanModal(false);
  };

  const handleOpenChatImports = () => {
    const stored = JSON.parse(localStorage.getItem(CHAT_IMPORTS_KEY) || '[]');
    setChatImports(stored);
    setShowChatImports((prev) => !prev);
  };

  const handleImportIdea = (idea) => {
    const importDate = selectedDate || new Date();
    const newDate = {
      id: Date.now(),
      date: importDate,
      title: idea.title || 'Chat Date Idea',
      location: 'From chat',
      time: '7:00 PM',
      type: 'special',
    };

    setPlannedDates((prev) => [newDate, ...prev]);

    const updated = chatImports.filter((item) => item.id !== idea.id);
    setChatImports(updated);
    localStorage.setItem(CHAT_IMPORTS_KEY, JSON.stringify(updated));
  };

  const handleScheduleFeedIdea = (idea) => {
    if (!selectedDate) return;
    const newDate = {
      id: Date.now(),
      date: selectedDate,
      title: idea.title || 'Saved Date Idea',
      location: 'Saved idea',
      time: '7:00 PM',
      type: 'special',
    };

    setPlannedDates((prev) => [newDate, ...prev]);

    const updated = feedSavedIdeas.filter((item) => item.id !== idea.id);
    setFeedSavedIdeas(updated);
    localStorage.setItem(FEED_SAVED_KEY, JSON.stringify(updated));
  };

  const handleScheduleRecommended = (idea) => {
    if (!selectedDate) return;

    const newDate = {
      id: Date.now(),
      date: selectedDate,
      title: idea.title || 'Saved Date Idea',
      location: idea.source === 'chat' ? 'From chat' : 'Saved idea',
      time: '7:00 PM',
      type: 'special',
    };

    setPlannedDates((prev) => [newDate, ...prev]);

    if (idea.source === 'chat') {
      const updated = chatImports.filter((item) => item.id !== idea.id);
      setChatImports(updated);
      localStorage.setItem(CHAT_IMPORTS_KEY, JSON.stringify(updated));
      return;
    }

    const updated = feedSavedIdeas.filter((item) => item.id !== idea.id);
    setFeedSavedIdeas(updated);
    localStorage.setItem(FEED_SAVED_KEY, JSON.stringify(updated));
  };

  const handleDeleteDate = (dateId) => {
    setPlannedDates((prev) => prev.filter((date) => date.id !== dateId));
  };

  const selectedDateDates = selectedDate 
    ? plannedDates.filter(d => d.date.toDateString() === selectedDate.toDateString())
    : [];

  const recommendedIdeas = [
    ...chatImports.map((idea) => ({
      ...idea,
      source: 'chat',
      title: idea.title || 'Chat Date Idea',
    })),
    ...feedSavedIdeas.map((idea) => ({
      ...idea,
      source: 'feed',
      title: idea.title || 'Saved Date Idea',
    })),
  ];

  const getTypeIcon = (type) => {
    switch(type) {
      case 'romantic': return '🕯️';
      case 'adventure': return '🎢';
      case 'cozy': return '🏠';
      case 'special': return '💕';
      default: return '📅';
    }
  };

  const getTypeClass = (type) => {
    return `date-type-${type}`;
  };

  const renderCalendarDays = () => {
    const days = [];
    
    for (let i = 0; i < firstDayOfMonth; i++) {
      days.push(<div key={`empty-${i}`} className="calendar-day empty"></div>);
    }
    
    for (let day = 1; day <= daysInMonth; day++) {
      const datesForDay = getDatesForDay(day);
      const hasDate = datesForDay.length > 0;
      
      days.push(
        <div
          key={day}
          className={`calendar-day ${isToday(day) ? 'today' : ''} ${isSelected(day) ? 'selected' : ''} ${hasDate ? 'has-date' : ''}`}
          onClick={() => handleDayClick(day)}
        >
          <span className="day-number">{day}</span>
          {hasDate && (
            <div className="day-dates">
              {datesForDay.slice(0, 2).map((date, idx) => (
                <div key={idx} className={`date-dot ${getTypeClass(date.type)}`} title={date.title}></div>
              ))}
              {datesForDay.length > 2 && <span className="more-dates">+{datesForDay.length - 2}</span>}
            </div>
          )}
        </div>
      );
    }
    
    return days;
  };

  const upcomingDates = plannedDates
    .filter(d => d.date >= new Date())
    .sort((a, b) => a.date - b.date)
    .slice(0, 3);

  return (
    <div className="calendar-page">
      <div className="calendar-header">
        <h1>📅 Date Calendar</h1>
        <p>Keep track of all your planned dates</p>
      </div>

      <div className="calendar-content">
        <div className="calendar-main">
          <div className="calendar-nav">
            <button className="nav-btn" onClick={prevMonth}>←</button>
            <h2>{monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}</h2>
            <button className="nav-btn" onClick={nextMonth}>→</button>
          </div>

          <div className="calendar-grid">
            {dayNames.map(day => (
              <div key={day} className="calendar-day-name">{day}</div>
            ))}
            {renderCalendarDays()}
          </div>

          <div className="calendar-legend">
            <div className="legend-item">
              <span className="legend-dot date-type-romantic"></span>
              <span>Romantic</span>
            </div>
            <div className="legend-item">
              <span className="legend-dot date-type-adventure"></span>
              <span>Adventure</span>
            </div>
            <div className="legend-item">
              <span className="legend-dot date-type-cozy"></span>
              <span>Cozy</span>
            </div>
            <div className="legend-item">
              <span className="legend-dot date-type-special"></span>
              <span>Special</span>
            </div>
          </div>
        </div>

        <div className="calendar-sidebar">
          {selectedDate ? (
            <div className="selected-date-panel">
              <h3>
                {selectedDate.toLocaleDateString('en-US', { 
                  weekday: 'long', 
                  month: 'long', 
                  day: 'numeric' 
                })}
              </h3>
              {selectedDateDates.length > 0 ? (
                <div className="selected-dates-list">
                  {selectedDateDates.map(date => (
                    <div key={date.id} className={`date-card ${getTypeClass(date.type)}`}>
                      <div className="date-card-icon">{getTypeIcon(date.type)}</div>
                      <div className="date-card-info">
                        <h4>{date.title}</h4>
                        <p className="date-location">📍 {date.location}</p>
                        <p className="date-time">🕐 {date.time}</p>
                      </div>
                      <button
                        className="date-delete-btn"
                        onClick={(event) => {
                          event.stopPropagation();
                          handleDeleteDate(date.id);
                        }}
                        aria-label={`Delete ${date.title}`}
                      >
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="no-dates-message">
                  <span className="no-dates-icon">💭</span>
                  <p>No dates planned for this day</p>
                  <button className="plan-date-btn" onClick={openPlanModal}>Plan a Date</button>
                </div>
              )}
            </div>
          ) : (
            <div className="upcoming-dates-panel">
              <h3>🔜 Upcoming Dates</h3>
              {upcomingDates.length > 0 ? (
                <div className="upcoming-dates-list">
                  {upcomingDates.map(date => (
                    <div key={date.id} className={`upcoming-date-card ${getTypeClass(date.type)}`}>
                      <div className="upcoming-date-day">
                        <span className="upcoming-day">{date.date.getDate()}</span>
                        <span className="upcoming-month">{monthNames[date.date.getMonth()].slice(0, 3)}</span>
                      </div>
                      <div className="upcoming-date-info">
                        <h4>{date.title}</h4>
                        <p>{date.location} • {date.time}</p>
                      </div>
                      <span className="upcoming-icon">{getTypeIcon(date.type)}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="no-dates-message">
                  <p>No upcoming dates</p>
                </div>
              )}
            </div>
          )}

          <div className="quick-actions-panel">
            <h3>⚡ Quick Actions</h3>
            <button className="action-btn primary" onClick={openPlanModal}>+ Add New Date</button>
            <button className="action-btn secondary" onClick={handleOpenChatImports}>
              Import from Chat{chatImports.length > 0 ? ` (${chatImports.length})` : ''}
            </button>
            {showChatImports && (
              <div className="chat-imports-panel">
                {chatImports.length > 0 ? (
                  chatImports.map((idea) => (
                    <div key={idea.id} className="chat-import-item">
                      <div className="chat-import-info">
                        <span className="chat-import-title">{idea.title}</span>
                        <span className="chat-import-meta">Saved from chat</span>
                      </div>
                      <button
                        className="chat-import-btn"
                        onClick={() => handleImportIdea(idea)}
                      >
                        Add
                      </button>
                    </div>
                  ))
                ) : (
                  <p className="chat-import-empty">No saved chat dates yet.</p>
                )}
              </div>
            )}
            {feedSavedIdeas.length > 0 && (
              <div className="chat-imports-panel">
                <div className="chat-import-section-title">Saved Date Ideas</div>
                {feedSavedIdeas.map((idea) => (
                  <div key={idea.id} className="chat-import-item">
                    <div className="chat-import-info">
                      <span className="chat-import-title">{idea.title}</span>
                      <span className="chat-import-meta">From feed</span>
                    </div>
                    <button
                      className="chat-import-btn"
                      onClick={() => handleScheduleFeedIdea(idea)}
                      disabled={!selectedDate}
                    >
                      {selectedDate ? 'Schedule' : 'Pick a day'}
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {selectedDate && selectedDateDates.length === 0 && recommendedIdeas.length > 0 && (
        <div className="unplanned-panel">
          <div className="unplanned-header">
            <h3>
              Ideas for{' '}
              {selectedDate.toLocaleDateString('en-US', {
                weekday: 'long',
                month: 'long',
                day: 'numeric',
              })}
            </h3>
            <p>Tap an idea to schedule it on this day.</p>
          </div>
          <div className="unplanned-list">
            {recommendedIdeas.map((idea) => (
              <button
                key={`${idea.source}-${idea.id}`}
                className="unplanned-card"
                onClick={() => handleScheduleRecommended(idea)}
              >
                <div className="unplanned-title">{idea.title}</div>
                <div className="unplanned-meta">
                  {idea.source === 'chat' ? 'From chat' : 'From feed'}
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

      {showPlanModal && (
        <div className="plan-modal-overlay" onClick={() => setShowPlanModal(false)}>
          <div className="plan-modal" onClick={(event) => event.stopPropagation()}>
            <div className="plan-modal-header">
              <h3>Plan a Date</h3>
              <button
                type="button"
                className="plan-modal-close"
                onClick={() => setShowPlanModal(false)}
                aria-label="Close plan date form"
              >
                ✕
              </button>
            </div>
            <form className="plan-form" onSubmit={handlePlanSubmit}>
              <label className="plan-field">
                Date
                <input
                  type="date"
                  name="date"
                  value={planForm.date}
                  onChange={handlePlanChange}
                  required
                />
              </label>
              <label className="plan-field">
                Title
                <input
                  type="text"
                  name="title"
                  value={planForm.title}
                  onChange={handlePlanChange}
                  placeholder="Movie night, picnic, dinner"
                  required
                />
              </label>
              <label className="plan-field">
                Location
                {isLoaded ? (
                  <Autocomplete
                    onLoad={handlePlanLocationLoad}
                    onPlaceChanged={handlePlanPlaceChanged}
                  >
                    <input
                      type="text"
                      name="location"
                      value={planForm.location}
                      onChange={handlePlanChange}
                      placeholder="Cafe, park, at home"
                      required
                    />
                  </Autocomplete>
                ) : (
                  <input
                    type="text"
                    name="location"
                    value=""
                    placeholder="Loading maps..."
                    readOnly
                    required
                  />
                )}
              </label>
              <div className="plan-row">
                <label className="plan-field">
                  Time
                  <input
                    type="time"
                    name="time"
                    value={planForm.time}
                    onChange={handlePlanChange}
                    required
                  />
                </label>
                <label className="plan-field">
                  Type
                  <select name="type" value={planForm.type} onChange={handlePlanChange}>
                    <option value="romantic">Romantic</option>
                    <option value="adventure">Adventure</option>
                    <option value="cozy">Cozy</option>
                    <option value="special">Special</option>
                  </select>
                </label>
              </div>
              <div className="plan-actions">
                <button type="button" className="plan-cancel" onClick={() => setShowPlanModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="plan-submit">
                  Save Date
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Calendar;
