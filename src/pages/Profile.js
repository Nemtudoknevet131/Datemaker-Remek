import React, { useRef, useState } from 'react';
import '../styles/Profile.css';

const Profile = () => {
  const avatarInputRef = useRef(null);
  const coverInputRef = useRef(null);
  const [isEditing, setIsEditing] = useState(false);
  const [activeCategory, setActiveCategory] = useState('All');
  const [isCategoryMenuOpen, setIsCategoryMenuOpen] = useState(false);
  const [profile, setProfile] = useState({
    name: 'Alex Johnson',
    age: 28,
    bio:"Hello I like picnics and im pretty cool hehe",
    coverPhoto: '/avatars/date2.png',
    favoriteDates: ['E-dating', "Picnics", "Beach dates", "Movies"],
    achievements: [
      {
        title: 'Date Night Planner',
        category: 'Time Related',
        description: 'Planned 5 successful dates in a row',
        icon: '🏆',
        date: 'Unlocked Jan 2026',
        unlocked: true,
      },
      {
        title: 'Profile Star',
        category: 'Profile',
        description: 'Completed 100% of profile details',
        icon: '✨',
        date: 'Unlocked Dec 2025',
        unlocked: true,
      },
      {
        title: 'Perfect Plan',
        category: 'Anniversary',
        description: 'Created 10 date plans',
        icon: '❤️',
        date: '',
        unlocked: false,
      },
      {
        title: 'Conversation Pro',
        category: 'Communication',
        description: 'Sent 100 messages',
        icon: '💬',
        date: '',
        unlocked: false,
      },
    ],
    photos: [
      '/avatars/female.jpg',
      '/avatars/male.jpeg',
      '/avatars/date1.jpg',
    ],
  });

  const [editForm, setEditForm] = useState({ ...profile });
  const achievementCategories = ['All', ...new Set(profile.achievements.map((achievement) => achievement.category))];
  const filteredAchievements = activeCategory === 'All'
    ? profile.achievements
    : profile.achievements.filter((achievement) => achievement.category === activeCategory);

  const handleAvatarClick = () => {
    avatarInputRef.current?.click();
  };

  const handleAvatarChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    const previewUrl = URL.createObjectURL(file);
    setProfile((prev) => ({
      ...prev,
      photos: [previewUrl, ...prev.photos.slice(1)],
    }));
    setEditForm((prev) => ({
      ...prev,
      photos: [previewUrl, ...prev.photos.slice(1)],
    }));
  };

  const handleCoverClick = () => {
    coverInputRef.current?.click();
  };

  const handleCoverChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    const previewUrl = URL.createObjectURL(file);
    setProfile((prev) => ({
      ...prev,
      coverPhoto: previewUrl,
    }));
    setEditForm((prev) => ({
      ...prev,
      coverPhoto: previewUrl,
    }));
  };

  const handleSave = () => {
    setProfile({ ...editForm });
    setIsEditing(false);
  };

  const handleCancel = () => {
    setEditForm({ ...profile });
    setIsEditing(false);
  };

  return (
    <div className="profile-page">
      <div className="profile-header">
        <div
          className="profile-cover"
          onClick={handleCoverClick}
          role="button"
          tabIndex={0}
        >
          <img src={profile.coverPhoto} alt="Profile cover" />
          <input
            ref={coverInputRef}
            type="file"
            accept="image/*"
            onChange={handleCoverChange}
            className="cover-input"
          />
        </div>
        <div className="profile-avatar-section">
          <div className="profile-avatar" onClick={handleAvatarClick} role="button" tabIndex={0}>
            <img src={profile.photos[0]} alt={profile.name} />
            <input
              ref={avatarInputRef}
              type="file"
              accept="image/*"
              onChange={handleAvatarChange}
              className="avatar-input"
            />
          </div>
          <div className="profile-basic-info">
            {isEditing ? (
              <>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                  className="edit-input name-input"
                />
                <input
                  type="number"
                  value={editForm.age}
                  onChange={(e) => setEditForm({ ...editForm, age: e.target.value })}
                  className="edit-input age-input"
                  placeholder="Age"
                />
              </>
            ) : (
              <>
                <h1 className="profile-name">{profile.name}, {profile.age}</h1>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="profile-content">
        <div className="profile-section">
          <div className="section-header">
            <h2>About Me</h2>
            {!isEditing && (
              <button className="edit-btn" onClick={() => setIsEditing(true)}>
                ✏️ Edit Profile
              </button>
            )}
          </div>
          {isEditing ? (
            <textarea
              value={editForm.bio}
              onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })}
              className="edit-textarea"
              rows={4}
            />
          ) : (
            <p className="profile-bio">{profile.bio}</p>
          )}
        </div>

        <div className="profile-section">
          <h2>Favorite Dates</h2>
          <div className="favorite-dates-grid">
            {profile.favoriteDates.map((favoriteDate, index) => (
              <span key={index} className="favorite-date-tag">
                {favoriteDate}
              </span>
            ))}
          </div>
        </div>

        <div className="profile-section">
          <h2>Unlocked Achievements</h2>
          <div className="achievement-box">
            <div className="achievement-header">
              <div className="achievement-title">
                <span className="achievement-icon">🔓</span>
                <span>Achievement Box</span>
              </div>
              <div className="achievement-actions">
                <span className="achievement-count">
                  {filteredAchievements.length} shown
                </span>
                <div className="achievement-sort">
                  <button
                    type="button"
                    className="sort-btn"
                    onClick={() => setIsCategoryMenuOpen((prev) => !prev)}
                    aria-expanded={isCategoryMenuOpen}
                  >
                    Sort
                    <span className="sort-caret">▾</span>
                  </button>
                  {isCategoryMenuOpen && (
                    <div className="sort-menu">
                      {achievementCategories.map((category) => (
                        <button
                          key={category}
                          type="button"
                          className={`sort-option ${activeCategory === category ? 'active' : ''}`}
                          onClick={() => {
                            setActiveCategory(category);
                            setIsCategoryMenuOpen(false);
                          }}
                        >
                          {category}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
            <div className="achievement-list">
              {filteredAchievements.map((achievement, index) => (
                <div
                  key={index}
                  className={`achievement-item ${achievement.unlocked ? '' : 'locked'}`}
                >
                  <div className="achievement-badge">
                    <span>{achievement.unlocked ? achievement.icon : '???'}</span>
                  </div>
                  <div className="achievement-info">
                    <h3>{achievement.unlocked ? achievement.title : 'Locked Achievement'}</h3>
                    <span className="achievement-category">{achievement.category}</span>
                    <p>
                      {achievement.unlocked
                        ? achievement.description
                        : 'Unlock this by completing more milestones.'}
                    </p>
                    {achievement.unlocked && (
                      <span className="achievement-date">{achievement.date}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="profile-section">
          <h2>My Photos</h2>
          <div className="photos-grid">
            {profile.photos.map((photo, index) => (
              <div key={index} className="photo-item">
                <img src={photo} alt={`Profile ${index + 1}`} />
              </div>
            ))}
            {isEditing && (
              <button className="add-photo-btn">
                <span>+</span>
                <span>Add Photo</span>
              </button>
            )}
          </div>
        </div>

        {isEditing && (
          <div className="edit-actions">
            <button className="save-btn" onClick={handleSave}>
              Save Changes
            </button>
            <button className="cancel-btn" onClick={handleCancel}>
              Cancel
            </button>
          </div>
        )}

        <div className="profile-stats">
          <div className="stat-item">
            <span className="stat-number">24</span>
            <span className="stat-label">Plans</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">12</span>
            <span className="stat-label">Dates</span>
          </div>
          <div className="stat-item">
            <span className="stat-number">8</span>
            <span className="stat-label">Shared Posts</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
