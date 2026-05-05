import React, { useEffect, useRef, useState } from 'react';
import { Autocomplete, useJsApiLoader } from '@react-google-maps/api';
import '../styles/Feed.css';

const libraries = ['places'];
const FEED_SAVED_KEY = 'datemakerFeedSavedIdeas';

const Feed = () => {
  const { isLoaded } = useJsApiLoader({
    googleMapsApiKey: process.env.REACT_APP_GOOGLE_MAPS_API_KEY || '',
    libraries,
  });

  const autocompleteRef = useRef(null);
  const [posts, setPosts] = useState([
    {
      id: 1,
      user: {
        name: 'Sarah Mitchell',
        avatar: '/avatars/default-female.svg',
        verified: true,
      },
      partner: {
        name: 'Jordan Lee',
        avatar: '/avatars/default-male.svg',
      },
      place: 'Skyline Rooftop Bar',
      content: 'Just planned our next rooftop date night downtown! 🌃✨ The city lights will be magical. Anyone else love sunset plans?',
      image: '/avatars/date1.jpg',
      likes: 234,
      savedCount: 20,
      timestamp: '2 hours ago',
      liked: false,
      saved: false,
    },
    {
      id: 2,
      user: {
        name: 'James Wilson',
        avatar: '/avatars/default-male.svg',
        verified: false,
      },
      partner: {
        name: 'Riley Chen',
        avatar: '/avatars/default-female.svg',
      },
      place: 'Summit Climbing Gym',
      content: "Pro tip for couples: don't be afraid to plan unique date ideas! Yesterday we went rock climbing together and it was so much fun! 🧗‍♂️💪",
      image: '/avatars/date2.png',
      likes: 189,
      savedCount: 20,
      timestamp: '5 hours ago',
      liked: true,
      saved: true,
    },
    {
      id: 3,
      user: {
        name: 'Emma Davis',
        avatar: '/avatars/default-female.svg',
        verified: true,
      },
      partner: {
        name: 'Noah Brooks',
        avatar: '/avatars/default-male.svg',
      },
      place: 'Riverside Trail + Gelato',
      content: "3 months of weekly date nights and counting! 💕 We plan everything right here on Datemaker. Here's to many more adventures together 🥂",
      image: '/avatars/date3.png',
      likes: 567,
      savedCount: 20,
      timestamp: '1 day ago',
      liked: false,
      saved: false,
    },
    {
      id: 4,
      user: {
        name: 'Michael Chen',
        avatar: '/avatars/default-male.svg',
        verified: false,
      },
      partner: {
        name: 'Avery Patel',
        avatar: '/avatars/default-female.svg',
      },
      place: 'Copper Mug Café',
      content: 'Coffee date essentials: good conversation, great vibes, and the perfect latte art ☕ What\'s your go-to cozy spot for a quick date?',
      image: '/avatars/date1.jpg',
      likes: 145,
      savedCount: 20,
      timestamp: '2 days ago',
      liked: false,
      saved: false,
    },
  ]);

  const [newPostContent, setNewPostContent] = useState('');
  const [partnerName, setPartnerName] = useState('');
  const [placeName, setPlaceName] = useState('');
  const [placeQuery, setPlaceQuery] = useState('');
  const [isLocationOpen, setIsLocationOpen] = useState(false);
  const [isFeelingOpen, setIsFeelingOpen] = useState(false);
  const [selectedFeeling, setSelectedFeeling] = useState('');
  const [selectedPhoto, setSelectedPhoto] = useState(null);
  const [savedIdeas, setSavedIdeas] = useState([
    { id: 1, name: 'Sunset Picnic', duration: '45 min', img: '/avatars/date1.jpg' },
    { id: 2, name: 'Museum + Coffee', duration: '2 hrs', img: '/avatars/date2.png' },
    { id: 3, name: 'Rooftop Dinner', duration: '3 hrs', img: '/avatars/date3.png' },
  ]);

  const fileInputRef = useRef(null);

  useEffect(() => {
    const stored = JSON.parse(localStorage.getItem(FEED_SAVED_KEY) || '[]');
    if (stored.length > 0) {
      const storedIds = new Set(stored.map((idea) => idea.id));
      setSavedIdeas((prev) => prev.filter((idea) => !storedIds.has(idea.id)));
    }
  }, []);

  const handleLike = (postId) => {
    setPosts(posts.map(post => {
      if (post.id === postId) {
        return {
          ...post,
          liked: !post.liked,
          likes: post.liked ? post.likes - 1 : post.likes + 1,
        };
      }
      return post;
    }));
  };

  const handleSavePost = (postId) => {
    setPosts(posts.map(post => {
      if (post.id === postId) {
        return {
          ...post,
          saved: !post.saved,
          savedCount: post.saved ? Math.max(0, post.savedCount - 1) : post.savedCount + 1,
        };
      }
      return post;
    }));
  };

  const handlePhotoClick = () => {
    fileInputRef.current?.click();
  };

  const handlePhotoChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    const previewUrl = URL.createObjectURL(file);
    setSelectedPhoto(previewUrl);
  };

  const handleLocationLoad = (autocomplete) => {
    autocompleteRef.current = autocomplete;
  };

  const handlePlaceChanged = () => {
    const autocomplete = autocompleteRef.current;
    if (!autocomplete) return;
    const place = autocomplete.getPlace();
    const name = place?.name || '';
    const address = place?.formatted_address || '';
    const finalPlace = name && address ? `${name}, ${address}` : name || address;
    if (finalPlace) {
      setPlaceName(finalPlace);
      setPlaceQuery(finalPlace);
    }
  };

  const handleSaveIdea = (idea) => {
    const stored = JSON.parse(localStorage.getItem(FEED_SAVED_KEY) || '[]');
    const newIdea = {
      id: idea.id,
      title: idea.name,
      duration: idea.duration,
      image: idea.img,
      createdAt: new Date().toISOString(),
    };
    const updated = [newIdea, ...stored];
    localStorage.setItem(FEED_SAVED_KEY, JSON.stringify(updated));
    setSavedIdeas((prev) => prev.filter((item) => item.id !== idea.id));
  };

  const handleNewPost = () => {
    if (newPostContent.trim()) {
      const finalContent = selectedFeeling
        ? `${newPostContent} ${selectedFeeling}`.trim()
        : newPostContent;
      const newPost = {
        id: Date.now(),
        user: {
          name: 'You',
          avatar: '/avatars/default-male.svg',
          verified: false,
        },
        partner: {
          name: partnerName.trim() || 'Partner',
          avatar: '/avatars/default-female.svg',
        },
        place: placeName.trim() || placeQuery.trim() || 'Date Spot',
        content: finalContent,
        image: selectedPhoto,
        likes: 0,
        savedCount: 0,
        timestamp: 'Just now',
        liked: false,
        saved: false,
      };
      setPosts([newPost, ...posts]);
      setNewPostContent('');
      setPartnerName('');
      setPlaceName('');
      setPlaceQuery('');
      setIsLocationOpen(false);
      setIsFeelingOpen(false);
      setSelectedFeeling('');
      setSelectedPhoto(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const feelings = [
    { label: 'Happy', emoji: '😊' },
    { label: 'Excited', emoji: '✨' },
    { label: 'Romantic', emoji: '💕' },
    { label: 'Playful', emoji: '😜' },
    { label: 'Cozy', emoji: '🛋️' },
    { label: 'Adventurous', emoji: '🏔️' },
    { label: 'Hungry', emoji: '🍕' },
    { label: 'Chill', emoji: '😌' },
  ];

  return (
    <div className="feed-page">
      <div className="feed-container">
        <div className="create-post-card">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="file-input"
            onChange={handlePhotoChange}
          />
          <div className="create-post-header">
            <img 
              src="/avatars/default-male.svg" 
              alt="Your avatar" 
              className="create-post-avatar"
            />
            <div className="create-post-fields">
              <textarea
                placeholder="Share your date plan or idea..."
                value={newPostContent}
                onChange={(e) => setNewPostContent(e.target.value)}
                className="create-post-input"
              />
              {selectedPhoto && (
                <div className="photo-preview">
                  <img src={selectedPhoto} alt="Selected" />
                </div>
              )}
              <input
                type="text"
                placeholder="Add your partner's name"
                value={partnerName}
                onChange={(e) => setPartnerName(e.target.value)}
                className="partner-input"
              />
              {isLocationOpen && (
                <div className="location-search">
                  {isLoaded ? (
                    <Autocomplete
                      onLoad={handleLocationLoad}
                      onPlaceChanged={handlePlaceChanged}
                    >
                      <input
                        type="text"
                        placeholder="Search a place"
                        value={placeQuery}
                        onChange={(e) => {
                          setPlaceQuery(e.target.value);
                          setPlaceName(e.target.value);
                        }}
                        className="partner-input"
                      />
                    </Autocomplete>
                  ) : (
                    <input
                      type="text"
                      placeholder="Loading maps..."
                      value=""
                      readOnly
                      className="partner-input"
                    />
                  )}
                </div>
              )}
            </div>
          </div>
          <div className="create-post-actions">
            <div className="post-attachments">
              <button className="attachment-btn" onClick={handlePhotoClick}>
                📷 Photo
              </button>
              <button
                className={`attachment-btn ${isLocationOpen ? 'active' : ''}`}
                onClick={() => setIsLocationOpen((prev) => !prev)}
              >
                📍 Location
              </button>
              <div className="feeling-picker">
                <button
                  className={`attachment-btn ${isFeelingOpen ? 'active' : ''}`}
                  onClick={() => setIsFeelingOpen((prev) => !prev)}
                >
                  {selectedFeeling ? selectedFeeling : '😊 Feeling'}
                </button>
                {isFeelingOpen && (
                  <div className="feeling-panel" role="menu">
                    {feelings.map((feeling) => (
                      <button
                        key={feeling.label}
                        className="feeling-option"
                        onClick={() => {
                          setSelectedFeeling(`${feeling.emoji} ${feeling.label}`);
                          setIsFeelingOpen(false);
                        }}
                        role="menuitem"
                      >
                        <span className="feeling-emoji">{feeling.emoji}</span>
                        <span className="feeling-label">{feeling.label}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
            <button 
              className="post-submit-btn"
              onClick={handleNewPost}
              disabled={!newPostContent.trim()}
            >
              Post
            </button>
          </div>
        </div>

        <div className="posts-list">
          {posts.map((post) => (
            <article key={post.id} className="post-card">
              <div className="post-header">
                <div className="post-user">
                  <div className="couple-avatars">
                    <img src={post.user.avatar} alt={post.user.name} className="post-avatar" />
                    <span className="couple-heart">❤️</span>
                    <img src={post.partner.avatar} alt={post.partner.name} className="post-avatar" />
                  </div>
                  <div className="post-user-info">
                    <span className="post-username">
                      {post.user.name}
                      {post.user.verified && <span className="verified-badge">✓</span>}
                      <span className="partner-name">&nbsp;·&nbsp;{post.partner.name}</span>
                    </span>
                    <span className="post-place">📍 {post.place}</span>
                    <span className="post-timestamp">{post.timestamp}</span>
                  </div>
                </div>
                <button className="post-menu-btn">•••</button>
              </div>

              <div className="post-content">
                <p>{post.content}</p>
              </div>

              {post.image && (
                <div className="post-image">
                  <img src={post.image} alt="Post content" />
                </div>
              )}

              <div className="post-stats">
                <span>{post.likes} likes</span>
                <span>{post.savedCount} saved</span>
              </div>

              <div className="post-actions">
                <button 
                  className={`action-btn ${post.liked ? 'liked' : ''}`}
                  onClick={() => handleLike(post.id)}
                >
                  {post.liked ? '❤️' : '🤍'} Like
                </button>
                <button
                  className={`action-btn ${post.saved ? 'saved' : ''}`}
                  onClick={() => handleSavePost(post.id)}
                >
                  {post.saved ? '🔖' : '📌'} Save
                </button>
              </div>
            </article>
          ))}
        </div>
      </div>

      <aside className="feed-sidebar">
        <div className="sidebar-card">
          <h3>Saved Date Ideas</h3>
          <div className="suggested-matches">
            {savedIdeas.length > 0 ? (
              savedIdeas.map((idea) => (
                <div key={idea.id} className="match-item">
                  <img src={idea.img} alt={idea.name} />
                  <div className="match-info">
                    <span className="match-name">{idea.name}</span>
                    <span className="match-status">{idea.duration}</span>
                  </div>
                  <button
                    className="match-btn"
                    onClick={() => handleSaveIdea(idea)}
                    aria-label={`Add ${idea.name} to calendar ideas`}
                  >
                    ⭐
                  </button>
                </div>
              ))
            ) : (
              <p className="match-empty">All set! Check the calendar to schedule your ideas.</p>
            )}
          </div>
        </div>

        <div className="sidebar-card">
          <h3>Trending Topics</h3>
          <div className="trending-list">
            <div className="trending-item">
              <span className="trending-tag">#DatePlanning</span>
              <span className="trending-count">1.2k posts</span>
            </div>
            <div className="trending-item">
              <span className="trending-tag">#DateNight</span>
              <span className="trending-count">856 posts</span>
            </div>
            <div className="trending-item">
              <span className="trending-tag">#CoupleGoals</span>
              <span className="trending-count">2.3k posts</span>
            </div>
            <div className="trending-item">
              <span className="trending-tag">#WeekendPlans</span>
              <span className="trending-count">432 posts</span>
            </div>
          </div>
        </div>
      </aside>
    </div>
  );
};

export default Feed;
