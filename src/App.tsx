import React, { useState, useEffect, useRef } from 'react';
import {
  Mic,
  MicOff,
  Volume2,
  VolumeX,
  PhoneOff,
  Radio,
  Zap,
  ShieldCheck,
  Smartphone,
  Code2,
  Copy,
  Check,
  Sparkles,
  ServerOff,
  Cpu,
  Layers,
  Activity,
  FileCode,
  Download,
  Flame,
  ArrowRight,
  Database,
  Trash2,
  CheckCircle2,
  Lock,
  Home,
  Users,
  Crown,
  User,
  Phone,
  Mail,
  MessageSquare,
  Search,
  UserPlus,
  Send,
  X,
  BookOpen,
  Headphones,
  Award,
  Globe,
  Settings,
  LogOut,
  ChevronRight,
  RefreshCw,
  Camera,
  Bell,
  BarChart3,
  HelpCircle,
  Clock,
  Star,
  Trophy,
  Video,
  Gift,
  Play,
  ChevronLeft,
  Flag,
  MoreHorizontal,
  Lightbulb,
  Coffee,
  Plane,
  Gamepad2,
  SlidersHorizontal,
  Briefcase,
  GraduationCap,
  Target
} from 'lucide-react';

interface FriendItem {
  id: string;
  name: string;
  level: string;
  status: 'Online' | 'In Call' | 'Offline';
  streak: number;
  avatarColor: string;
  location: string;
  country?: string;
  subtitle?: string;
  tone?: string;
}

interface ChatMessage {
  id: string;
  sender: 'me' | 'them';
  text: string;
  time: string;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'code' | 'architecture'>('simulator');
  const [selectedFileKey, setSelectedFileKey] = useState<string>('MainActivity.kt');
  const [copied, setCopied] = useState<boolean>(false);

  // Phone App State Machine
  const [currentAppTab, setCurrentAppTab] = useState<'HOME' | 'FRIENDS' | 'SUBSCRIPTION' | 'PROFILE'>('HOME');
  const [simState, setSimState] = useState<'IDLE' | 'SEARCHING' | 'CONNECTING' | 'IN_CALL' | 'ENDED'>('IDLE');
  
  // User Profile State
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userName, setUserName] = useState('Guest Learner');
  const [userEmail, setUserEmail] = useState('');
  const [profileImage, setProfileImage] = useState<string | null>(() => {
    return localStorage.getItem('speakfree_profile_image') || null;
  });
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isProUser, setIsProUser] = useState(false);
  const [showPlayBillingSheet, setShowPlayBillingSheet] = useState(false);
  const [isVerifyingReceipt, setIsVerifyingReceipt] = useState(false);
  const [playOrderId, setPlayOrderId] = useState('GPA.4491-8271-9014-55102');
  const [playExpiryDate, setPlayExpiryDate] = useState('05 Feb 2027');
  const [userStreak, setUserStreak] = useState(0);
  const [selectedEnglishLevel, setSelectedEnglishLevel] = useState<'Beginner' | 'Intermediate' | 'Advanced'>('Intermediate');
  const [topicSetIndex, setTopicSetIndex] = useState(0);
  const [friendsPane, setFriendsPane] = useState<'all' | 'requests' | 'search'>('all');
  const [friendSearchQuery, setFriendSearchQuery] = useState('');
  const [showSplash, setShowSplash] = useState(true);
  const [splashProgress, setSplashProgress] = useState(8);

  // Modal Dialog States
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [showPrivacyScreen, setShowPrivacyScreen] = useState(false);
  const [authStep, setAuthStep] = useState<'login' | 'signup' | 'verify'>('login');
  const [inputName, setInputName] = useState('');
  const [inputEmail, setInputEmail] = useState('');
  const [inputPassword, setInputPassword] = useState('');
  const [authError, setAuthError] = useState('');
  const [authSuccess, setAuthSuccess] = useState('');

  // Active Chat State
  const [activeChatFriend, setActiveChatFriend] = useState<FriendItem | null>(null);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [newMsgText, setNewMsgText] = useState('');

  // Search & Call Simulation State
  const [searchSecs, setSearchSecs] = useState(0);
  const [callSecs, setCallSecs] = useState(0);
  const [isLimitReached, setIsLimitReached] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [isSpeaker, setIsSpeaker] = useState(true);
  const [currentPartnerName, setCurrentPartnerName] = useState('Rahul Verma (Delhi)');
  const [currentPartnerId, setCurrentPartnerId] = useState('8F2A');
  const [dbLogs, setDbLogs] = useState<Array<{ time: string; action: string; type: 'write' | 'read' | 'delete' | 'info' }>>([
    { time: '09:14:02.120', action: 'App initialized with Zero-Cost Firestore P2P Signaling', type: 'info' }
  ]);

  // Friends Data
  const [friendsList, setFriendsList] = useState<FriendItem[]>([
    { id: '1042', name: 'Rohan', level: 'Intermediate', status: 'In Call', streak: 7, avatarColor: 'bg-[#d6ecff]', location: 'India', country: 'IN', subtitle: 'Practicing now...' },
    { id: '3910', name: 'Emma', level: 'Advanced', status: 'Online', streak: 14, avatarColor: 'bg-[#ffe4ef]', location: 'UK', country: 'UK', subtitle: 'Online' },
    { id: '5582', name: 'Liam', level: 'Beginner', status: 'Offline', streak: 4, avatarColor: 'bg-[#d6ecff]', location: 'USA', country: 'US', subtitle: 'Last seen 2 hours ago' },
    { id: '8821', name: 'Aisha', level: 'Intermediate', status: 'Online', streak: 12, avatarColor: 'bg-[#ffe8d6]', location: 'India', country: 'IN', subtitle: "Let's practice together!" },
  ]);
  const [suggestedFriends, setSuggestedFriends] = useState<FriendItem[]>([
    { id: 's1', name: 'Alex', level: 'Intermediate', status: 'Online', streak: 3, avatarColor: 'bg-[#d6ecff]', location: 'USA', country: 'US', tone: 'bg-[#eaf3ff]' },
    { id: 's2', name: 'Sara', level: 'Beginner', status: 'Online', streak: 2, avatarColor: 'bg-[#ffe4ef]', location: 'UK', country: 'UK', tone: 'bg-[#fff0f4]' },
    { id: 's3', name: 'Kenji', level: 'Advanced', status: 'Online', streak: 9, avatarColor: 'bg-[#e6f8ef]', location: 'Japan', country: 'JP', tone: 'bg-[#eafaf1]' },
    { id: 's4', name: 'Priya', level: 'Intermediate', status: 'Online', streak: 6, avatarColor: 'bg-[#fff4d6]', location: 'India', country: 'IN', tone: 'bg-[#fff8e8]' },
  ]);
  const [newFriendIdInput, setNewFriendIdInput] = useState('');
  const [friendAddSuccess, setFriendAddSuccess] = useState(false);

  const searchTimerRef = useRef<NodeJS.Timeout | null>(null);
  const callTimerRef = useRef<NodeJS.Timeout | null>(null);

  const addLog = (action: string, type: 'write' | 'read' | 'delete' | 'info') => {
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0] + '.' + String(now.getMilliseconds()).padStart(3, '0');
    setDbLogs(prev => [{ time: timeStr, action, type }, ...prev.slice(0, 19)]);
  };

  // Clear auth messages when modal closes
  useEffect(() => {
    if (!showAuthModal) {
      setAuthError('');
      setAuthSuccess('');
    }
  }, [showAuthModal]);

  useEffect(() => {
    if (!showSplash) return;
    setSplashProgress(8);
    const started = Date.now();
    const duration = 2400;
    const tick = window.setInterval(() => {
      const pct = Math.min(100, 8 + ((Date.now() - started) / duration) * 92);
      setSplashProgress(pct);
      if (pct >= 100) {
        window.clearInterval(tick);
        window.setTimeout(() => setShowSplash(false), 220);
      }
    }, 40);
    return () => window.clearInterval(tick);
  }, [showSplash]);

  // Handle Find Partner click
  const handleStartSearch = () => {
    setSimState('SEARCHING');
    setSearchSecs(0);
    setIsLimitReached(false);
    setIsMuted(false);
    setIsSpeaker(true);
    addLog('Matchmaking request: generated local ephemeral ID', 'info');
    addLog('CREATE /waiting_room/anon_user {status: "searching", level: "' + selectedEnglishLevel + '"}', 'write');

    let count = 0;
    searchTimerRef.current = setInterval(() => {
      count++;
      setSearchSecs(count);

      // Match found after ~2.5 seconds
      if (count === 3) {
        if (searchTimerRef.current) clearInterval(searchTimerRef.current);
        setSimState('CONNECTING');
        const partners = ['Rahul Verma (Delhi)', 'Simran Kaur (Chandigarh)', 'Amit Deshmukh (Pune)', 'Kavya Nair (Kerala)'];
        const chosen = partners[Math.floor(Math.random() * partners.length)];
        const randId = Math.floor(1000 + Math.random() * 9000).toString(16).toUpperCase();
        setCurrentPartnerName(chosen);
        setCurrentPartnerId(randId);
        addLog(`MATCH FOUND with learner: ${chosen} (#${randId})`, 'info');
        addLog('CREATE /rooms/room_9941 {callerId, calleeId, offer}', 'write');

        setTimeout(() => {
          setSimState('IN_CALL');
          setCallSecs(0);
          addLog('STUN Binding Success -> WebRTC Audio: CONNECTED', 'info');
          addLog('⚡ PURGE /waiting_room/anon_user (0 Firestore Storage)', 'delete');
          addLog('⚡ PURGE /rooms/room_9941 (Signaling Docs Cleaned)', 'delete');
          addLog('🔒 Firestore snapshot listeners detached (0 active reads)', 'info');

          let callCount = 0;
          callTimerRef.current = setInterval(() => {
            callCount++;
            setCallSecs(callCount);

            // Free user limit: 10 minutes (600 seconds)
            if (!isProUser && callCount >= 600) {
              if (callTimerRef.current) clearInterval(callTimerRef.current);
              setIsLimitReached(true);
              setSimState('ENDED');
              addLog('⏱️ Free 10-min session limit reached. Call automatically disconnected.', 'info');
            }
          }, 1000);
        }, 1200);
      }
    }, 1000);
  };

  const handleStartDirectCall = (friend: FriendItem) => {
    setCurrentPartnerName(friend.name);
    setCurrentPartnerId(friend.id);
    setSimState('CONNECTING');
    setIsLimitReached(false);
    setIsMuted(false);
    setIsSpeaker(true);
    addLog(`Direct Call ringing peer: ${friend.name}`, 'write');

    setTimeout(() => {
      setSimState('IN_CALL');
      setCallSecs(0);
      addLog(`P2P Audio Connected with friend ${friend.name}`, 'info');

      let callCount = 0;
      callTimerRef.current = setInterval(() => {
        callCount++;
        setCallSecs(callCount);

        // Free user limit: 10 minutes (600 seconds)
        if (!isProUser && callCount >= 600) {
          if (callTimerRef.current) clearInterval(callTimerRef.current);
          setIsLimitReached(true);
          setSimState('ENDED');
          addLog(`⏱️ Free 10-min limit reached with friend ${friend.name}. Call ended.`, 'info');
        }
      }, 1000);
    }, 1500);
  };

  const handleCancelSearch = () => {
    if (searchTimerRef.current) clearInterval(searchTimerRef.current);
    addLog('DELETE /waiting_room/anon_user (search cancelled)', 'delete');
    setSimState('IDLE');
    setSearchSecs(0);
  };

  const handleEndCall = (isLimit: boolean = false) => {
    if (callTimerRef.current) clearInterval(callTimerRef.current);
    addLog('PeerConnection closed. Audio hardware released.', 'info');
    setIsLimitReached(isLimit);
    setSimState('ENDED');
    if (!isLimit) {
      setTimeout(() => {
        setSimState('IDLE');
        setCallSecs(0);
      }, 1800);
    }
  };

  // Quick testing helpers for 10-min limit simulation
  const handleFastForward = (secondsToAdd: number) => {
    setCallSecs(prev => {
      const next = prev + secondsToAdd;
      if (!isProUser && next >= 600) {
        if (callTimerRef.current) clearInterval(callTimerRef.current);
        setIsLimitReached(true);
        setSimState('ENDED');
        addLog('⏱️ Free 10-min limit reached (via fast-forward). Call disconnected.', 'info');
        return 600;
      }
      return next;
    });
  };

  const handleJumpToLimit = () => {
    setCallSecs(590); // 9 mins 50 secs (10 secs remaining)
    addLog('⏩ Jumped to 09:50 to observe 10-minute auto-disconnect!', 'info');
  };

  // Profile Photo from Gallery Handler
  const handleProfileImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      addLog('⚠️ Image file is too large (maximum 5MB allowed)', 'info');
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      const dataUrl = event.target?.result as string;
      if (dataUrl) {
        setProfileImage(dataUrl);
        try {
          localStorage.setItem('speakfree_profile_image', dataUrl);
        } catch (_err) {
          // localStorage quota catch
        }
        addLog('📸 Profile photo successfully selected from gallery!', 'write');
      }
    };
    reader.readAsDataURL(file);
    e.target.value = '';
  };

  // Auth Functions
  const handleEmailSignUp = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputEmail || !inputPassword || !inputName) {
      setAuthError('Please fill in all fields');
      return;
    }
    if (inputPassword.length < 6) {
      setAuthError('Password must be at least 6 characters');
      return;
    }
    setAuthError('');
    setIsLoggedIn(true);
    setUserName(inputName);
    setUserEmail(inputEmail);
    setShowAuthModal(false);
    addLog(`Firebase Auth: createUserWithEmailAndPassword() successful for ${inputEmail}`, 'write');
    addLog(`Firestore: Initialized secure profile under /Users/${inputEmail}`, 'write');
  };

  const handleEmailLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputEmail || !inputPassword) {
      setAuthError('Please enter your email and password');
      return;
    }
    setAuthError('');
    setIsLoggedIn(true);
    // Use part before @ as display name if nameInput is empty
    const defaultName = inputEmail.split('@')[0];
    setUserName(inputName.trim() || defaultName || 'Learner');
    setUserEmail(inputEmail);
    setShowAuthModal(false);
    addLog(`Firebase Auth: signInWithEmailAndPassword() successful for ${inputEmail}`, 'read');
    addLog(`Firestore: Initialized secure profile under /Users/${inputEmail}`, 'write');
  };

  const handleConfirmVerify = () => {
    setAuthError('');
    setAuthSuccess('');
    setAuthStep('login');
  };

  const handleOpenChat = (friend: FriendItem) => {
    setActiveChatFriend(friend);
    setChatMessages([
      { id: '1', sender: 'them', text: `Hi! Ready to practice English conversation today?`, time: '10:15 AM' },
      { id: '2', sender: 'me', text: `Hey ${friend.name.split(' ')[0]}! Yes, let's practice the job interview topic!`, time: '10:16 AM' }
    ]);
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMsgText.trim() || !activeChatFriend) return;
    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      sender: 'me',
      text: newMsgText.trim(),
      time: 'Just now'
    };
    setChatMessages(prev => [...prev, userMsg]);
    setNewMsgText('');

    setTimeout(() => {
      const reply: ChatMessage = {
        id: (Date.now() + 1).toString(),
        sender: 'them',
        text: 'That sounds great! Would you like to do a quick voice call now?',
        time: 'Just now'
      };
      setChatMessages(prev => [...prev, reply]);
    }, 1200);
  };

  const handleAddFriend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFriendIdInput.trim()) return;
    const newFriend: FriendItem = {
      id: newFriendIdInput.trim().toUpperCase(),
      name: 'Learner #' + newFriendIdInput.trim().toUpperCase(),
      level: 'Intermediate',
      status: 'Online',
      streak: 1,
      avatarColor: 'bg-emerald-600',
      location: 'India'
    };
    setFriendsList(prev => [newFriend, ...prev]);
    setNewFriendIdInput('');
    setFriendAddSuccess(true);
    setTimeout(() => setFriendAddSuccess(false), 3000);
    addLog(`Friend added: ${newFriend.name}`, 'info');
  };

  const handleOpenPlayBilling = () => {
    if (!isLoggedIn) {
      setShowAuthModal(true);
      return;
    }
    setShowPlayBillingSheet(true);
  };

  const handleConfirmPlayPurchase = () => {
    setIsVerifyingReceipt(true);
    addLog('Google Play Billing v7: BillingClient.launchBillingFlow(speakfree_vip_5months)', 'info');

    setTimeout(() => {
      const generatedOrderId = `GPA.${Math.floor(1000 + Math.random() * 9000)}-${Math.floor(1000 + Math.random() * 9000)}-${Math.floor(1000 + Math.random() * 9000)}-${Math.floor(10000 + Math.random() * 90000)}`;
      const token = `pbtok_${Math.random().toString(36).substring(2, 12)}_${Date.now()}`;
      
      const expiry = new Date();
      expiry.setMonth(expiry.getMonth() + 5);
      const formattedExpiry = expiry.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });

      setPlayOrderId(generatedOrderId);
      setPlayExpiryDate(formattedExpiry);
      setIsProUser(true);
      setIsVerifyingReceipt(false);
      setShowPlayBillingSheet(false);

      addLog(`Google Play: Purchase state PURCHASED (${generatedOrderId})`, 'info');
      addLog(`BillingClient: acknowledgePurchase() verified token ${token.substring(0, 16)}...`, 'write');
      addLog(`Firestore: Synced verified Google Play receipt under /Users/${userEmail || 'learner'}`, 'write');
    }, 1200);
  };

  const formatTimer = (totalSeconds: number) => {
    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  };

  const isLightPhone = showSplash || simState !== 'IDLE' || currentAppTab === 'PROFILE' || currentAppTab === 'SUBSCRIPTION' || currentAppTab === 'FRIENDS' || currentAppTab === 'HOME';
  const topicSets = [
    [
      { icon: Coffee, title: 'Daily Life', desc: 'Talk about your routine' },
      { icon: Plane, title: 'Travel', desc: 'Share your travel experiences' },
      { icon: Gamepad2, title: 'Hobbies', desc: 'Discuss your interests' }
    ],
    [
      { icon: BookOpen, title: 'Job Interview', desc: 'Tell me about yourself' },
      { icon: Globe, title: 'Culture', desc: 'Food, festivals & cities' },
      { icon: Headphones, title: 'Music', desc: 'Songs that you love' }
    ]
  ];
  const activeTopics = topicSets[topicSetIndex % topicSets.length];
  const partnerFirstName = currentPartnerName.split(' ')[0] || 'Alex';
  const partnerLocation = currentPartnerName.includes('(')
    ? currentPartnerName.slice(currentPartnerName.indexOf('(') + 1, currentPartnerName.indexOf(')'))
    : 'USA';

  return (
    <div className="min-h-screen bg-[#080a0e] text-slate-100 flex flex-col selection:bg-emerald-500/20 selection:text-emerald-300">
      {/* Top Navigation Header */}
      <header className="border-b border-slate-800/80 bg-[#0c0f15]/95 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl overflow-hidden shadow-lg shadow-sky-900/40 border border-sky-400/30 shrink-0">
              <img src="/logo.png" alt="SpeakFree App Logo" className="w-full h-full object-cover" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="font-bold text-base sm:text-lg text-white tracking-tight">SpeakFree Live</h1>
                <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  Android Native • $0/mo
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">Full English Speaking App • Email & Password Login • WebRTC Voice</p>
            </div>
          </div>

          {/* Navigation Mode Switcher */}
          <div className="flex items-center bg-slate-900/90 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('simulator')}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all ${
                activeTab === 'simulator'
                  ? 'bg-emerald-500 text-slate-950 font-semibold shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              Live App Preview
            </button>
            <button
              onClick={() => setActiveTab('code')}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all ${
                activeTab === 'code'
                  ? 'bg-emerald-500 text-slate-950 font-semibold shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Code2 className="w-3.5 h-3.5" />
              Kotlin Codebase
            </button>
            <button
              onClick={() => setActiveTab('architecture')}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all ${
                activeTab === 'architecture'
                  ? 'bg-emerald-500 text-slate-950 font-semibold shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Zap className="w-3.5 h-3.5" />
              Zero-Cost Guide
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 lg:p-8">
        {activeTab === 'simulator' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            
            {/* Left Column: Android Interactive Phone Mockup */}
            <div className="lg:col-span-5 flex flex-col items-center">
              
              {/* Quick Screen Switcher Toolbar */}
              <div className="w-full max-w-[360px] flex items-center justify-between mb-3 px-2 py-1.5 rounded-xl bg-slate-900/80 border border-slate-800 text-xs text-slate-300">
                <span className="text-[11px] text-slate-400 font-medium">Phone Screen:</span>
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => { setSimState('IDLE'); setCurrentAppTab('HOME'); setShowSplash(true); }}
                    className={`px-2 py-1 rounded text-[11px] font-semibold transition-all ${
                      showSplash ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 text-slate-300 hover:text-white'
                    }`}
                  >
                    Splash
                  </button>
                  <button
                    onClick={() => { setShowSplash(false); setSimState('IDLE'); setCurrentAppTab('HOME'); }}
                    className={`px-2 py-1 rounded text-[11px] font-semibold transition-all ${
                      !showSplash && simState === 'IDLE' && currentAppTab === 'HOME' ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 text-slate-300 hover:text-white'
                    }`}
                  >
                    Home
                  </button>
                  <button
                    onClick={() => { setShowSplash(false); setSimState('IDLE'); setCurrentAppTab('FRIENDS'); }}
                    className={`px-2 py-1 rounded text-[11px] font-semibold transition-all ${
                      simState === 'IDLE' && currentAppTab === 'FRIENDS' ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 text-slate-300 hover:text-white'
                    }`}
                  >
                    Friends
                  </button>
                  <button
                    onClick={() => { setShowSplash(false); setSimState('IDLE'); setCurrentAppTab('SUBSCRIPTION'); }}
                    className={`px-2 py-1 rounded text-[11px] font-semibold transition-all ${
                      simState === 'IDLE' && currentAppTab === 'SUBSCRIPTION' ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 text-slate-300 hover:text-white'
                    }`}
                  >
                    5-Mo Plan
                  </button>
                  <button
                    onClick={() => { setShowSplash(false); setSimState('IDLE'); setCurrentAppTab('PROFILE'); }}
                    className={`px-2 py-1 rounded text-[11px] font-semibold transition-all ${
                      simState === 'IDLE' && currentAppTab === 'PROFILE' ? 'bg-emerald-500 text-slate-950' : 'bg-slate-800 text-slate-300 hover:text-white'
                    }`}
                  >
                    Profile
                  </button>
                </div>
              </div>

              {/* Phone Device Shell */}
              <div className="w-full max-w-[360px] h-[720px] bg-[#0b0e14] rounded-[44px] border-[6px] border-slate-800 shadow-2xl shadow-emerald-950/20 overflow-hidden relative flex flex-col select-none ring-1 ring-white/10">
                
                {/* Phone Top Notch Bar */}
                <div className={`h-7 w-full flex items-center justify-between px-6 pt-2 z-30 ${isLightPhone ? 'bg-[#eef3fb]' : 'bg-[#0b0e14]'}`}>
                  <span className={`text-[11px] font-semibold ${isLightPhone ? 'text-slate-800' : 'text-slate-300'}`}>9:41</span>
                  <div className={`w-16 h-3.5 rounded-full mx-auto ${isLightPhone ? 'bg-slate-900/80' : 'bg-black'}`} />
                  <div className={`flex items-center gap-1.5 text-[10px] ${isLightPhone ? 'text-slate-800' : 'text-slate-300'}`}>
                    <span className={`font-bold ${isLightPhone ? 'text-slate-800' : 'text-emerald-400'}`}>5G</span>
                    <div className={`w-3.5 h-2 border rounded-xs flex items-center p-0.5 ${isLightPhone ? 'border-slate-700' : 'border-slate-400'}`}>
                      <div className={`w-full h-full ${isLightPhone ? 'bg-slate-800' : 'bg-emerald-400'}`} />
                    </div>
                  </div>
                </div>

                {/* Main Inside Phone Container */}
                <div className={`flex-1 flex flex-col relative overflow-hidden ${isLightPhone ? 'bg-[#eef3fb]' : 'bg-[#0a0d13]'}`}>
                  {showSplash && (
                    <div className="absolute inset-0 z-50 bg-gradient-to-b from-[#eaf4ff] via-[#f4f8ff] to-[#e8f2ff] flex flex-col items-center overflow-hidden">
                      <div className="absolute -top-10 -left-16 w-48 h-48 rounded-full bg-[#d6e8ff]/70" />
                      <div className="absolute top-24 -right-10 w-36 h-36 rounded-full bg-[#cfe4ff]/60" />
                      <div className="absolute bottom-16 -left-10 w-40 h-28 rounded-full bg-[#dbeafe]/80" />
                      <div className="absolute bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-[#cfe4ff] to-transparent" />

                      <div className="flex-1 w-full flex flex-col items-center px-6 pt-10 pb-5 relative">
                        <div className="relative mt-4 mb-2">
                          <div className="w-[168px] h-[168px] rounded-full bg-[#d6e8ff] flex items-center justify-center">
                            <img src="/avatar-anand.svg" alt="SpeakFree" className="w-[148px] h-[148px] object-contain" />
                          </div>
                          <div className="absolute left-[-18px] top-10 w-11 h-11 rounded-2xl bg-white shadow-md flex items-center justify-center text-[#3d6ef5]">
                            <MessageSquare className="w-5 h-5" />
                          </div>
                          <div className="absolute right-[-22px] top-16 w-14 h-14 rounded-full bg-white shadow-md flex items-center justify-center text-[#3d6ef5]">
                            <Mic className="w-6 h-6" />
                          </div>
                        </div>

                        <h1 className="text-[32px] leading-none font-black tracking-tight mt-2">
                          <span className="text-[#1b2559]">Speak</span>
                          <span className="text-[#3d6ef5]">Free</span>
                        </h1>
                        <div className="w-16 h-[6px] rounded-full bg-[#3d6ef5] mt-2 mb-3" />
                        <p className="text-[13px] text-[#8b95b7] text-center font-medium leading-snug">
                          Speak. Practice. Make Friends.<br />Be Confident.
                        </p>

                        <div className="w-[210px] mt-6">
                          <div className="h-[8px] rounded-full bg-[#d7e4ff] overflow-hidden">
                            <div
                              className="h-full rounded-full bg-gradient-to-r from-[#3d6ef5] to-[#60a5fa] transition-[width] duration-75"
                              style={{ width: `${splashProgress}%` }}
                            />
                          </div>
                          <p className="text-[11px] text-[#8b95b7] text-center mt-2 font-medium">Loading...</p>
                        </div>

                        <div className="mt-auto w-full flex items-start justify-between px-4 pt-6">
                          <div className="flex flex-col items-center w-[72px]">
                            <div className="w-11 h-11 rounded-full bg-[#e6f8ef] flex items-center justify-center text-[#22c55e]">
                              <Mic className="w-5 h-5" />
                            </div>
                            <span className="text-[11px] font-bold text-[#5b6b8c] mt-1.5">Practice</span>
                          </div>
                          <div className="flex flex-col items-center w-[72px]">
                            <div className="w-11 h-11 rounded-full bg-[#ece8ff] flex items-center justify-center text-[#7b61ff]">
                              <Users className="w-5 h-5" />
                            </div>
                            <span className="text-[11px] font-bold text-[#5b6b8c] mt-1.5">Make Friends</span>
                          </div>
                          <div className="flex flex-col items-center w-[72px]">
                            <div className="w-11 h-11 rounded-full bg-[#fff4d6] flex items-center justify-center text-[#f5a623]">
                              <BarChart3 className="w-5 h-5" />
                            </div>
                            <span className="text-[11px] font-bold text-[#5b6b8c] mt-1.5">Improve</span>
                          </div>
                        </div>

                        <p className="text-[9px] tracking-[0.18em] font-bold text-[#a8b4d0] mt-5">YOUR VOICE, A BRIGHTER YOU</p>
                      </div>
                    </div>
                  )}

                  {/* Active Call / Searching Overlay */}
                  {simState !== 'IDLE' ? (
                    <div className="flex-1 flex flex-col bg-[#eef3fb] z-20 overflow-hidden">
                      
                      {simState === 'SEARCHING' && (
                        <div className="flex-1 flex flex-col px-4 pt-3 pb-5">
                          <div className="flex items-start justify-between">
                            <div className="flex items-start gap-1">
                              <button
                                onClick={handleCancelSearch}
                                className="mt-0.5 w-8 h-8 rounded-full flex items-center justify-center text-[#1b2559]"
                              >
                                <ChevronLeft className="w-5 h-5" strokeWidth={2.4} />
                              </button>
                              <div>
                                <h2 className="text-[20px] leading-none font-extrabold text-[#1b2559]">Live Practice Call</h2>
                                <p className="text-[11px] text-[#8b95b7] mt-1.5 font-medium">Talk • Practice • Improve</p>
                              </div>
                            </div>
                            <button
                              onClick={handleCancelSearch}
                              className="px-3 py-1.5 rounded-full bg-[#ffe8f0] text-[#f43f5e] text-[11px] font-bold flex items-center gap-1"
                            >
                              <Flag className="w-3 h-3" />
                              End Practice
                            </button>
                          </div>
                          <div className="flex-1 flex flex-col items-center justify-center text-center">
                            <div className="relative flex items-center justify-center my-4">
                              <div className="absolute w-36 h-36 rounded-full border-2 border-[#3d6ef5]/20 animate-ping" style={{ animationDuration: '2s' }} />
                              <div className="w-[88px] h-[88px] rounded-full bg-white border-[3px] border-[#d6e4ff] flex items-center justify-center shadow-sm">
                                <Radio className="w-8 h-8 text-[#3d6ef5] animate-spin" style={{ animationDuration: '8s' }} />
                              </div>
                            </div>
                            <span className="px-3 py-1 rounded-full bg-[#e8f0ff] text-[#3d6ef5] text-[11px] font-bold mb-2">
                              Matching Level: {selectedEnglishLevel}
                            </span>
                            <h3 className="text-[16px] font-extrabold text-[#1b2559]">Searching for Partner...</h3>
                            <p className="text-[11px] text-[#8b95b7] mt-1">Connecting with an active learner ({searchSecs}s)</p>
                            <button
                              onClick={handleCancelSearch}
                              className="mt-6 px-5 py-2.5 rounded-full bg-white text-[#1b2559] text-xs font-bold shadow-sm"
                            >
                              Cancel Search
                            </button>
                          </div>
                        </div>
                      )}

                      {simState === 'CONNECTING' && (
                        <div className="flex-1 flex flex-col px-4 pt-3 pb-5">
                          <div className="flex items-start gap-1">
                            <button
                              onClick={() => handleEndCall(false)}
                              className="mt-0.5 w-8 h-8 rounded-full flex items-center justify-center text-[#1b2559]"
                            >
                              <ChevronLeft className="w-5 h-5" strokeWidth={2.4} />
                            </button>
                            <div>
                              <h2 className="text-[20px] leading-none font-extrabold text-[#1b2559]">Live Practice Call</h2>
                              <p className="text-[11px] text-[#8b95b7] mt-1.5 font-medium">Talk • Practice • Improve</p>
                            </div>
                          </div>
                          <div className="flex-1 flex flex-col items-center justify-center text-center">
                            <div className="w-[88px] h-[88px] rounded-full bg-white p-[3px] shadow-sm mb-3 overflow-hidden">
                              <img src="/avatar-anand.svg" alt="" className="w-full h-full object-cover rounded-full bg-[#d6ecff]" />
                            </div>
                            <h3 className="text-[16px] font-extrabold text-[#1b2559]">Connecting with {partnerFirstName}...</h3>
                            <p className="text-[11px] text-[#8b95b7] mt-1">Establishing secure voice connection...</p>
                          </div>
                        </div>
                      )}

                      {simState === 'IN_CALL' && (
                        <div className="flex-1 flex flex-col px-3.5 pt-2 pb-3 overflow-y-auto">
                          <div className="flex items-start justify-between">
                            <div className="flex items-start gap-0.5 min-w-0">
                              <button
                                onClick={() => handleEndCall(false)}
                                className="mt-0.5 w-7 h-7 rounded-full flex items-center justify-center text-[#1b2559] shrink-0"
                              >
                                <ChevronLeft className="w-5 h-5" strokeWidth={2.4} />
                              </button>
                              <div className="min-w-0">
                                <h2 className="text-[18px] leading-none font-extrabold text-[#1b2559]">Live Practice Call</h2>
                                <p className="text-[10px] text-[#8b95b7] mt-1 font-medium">Talk • Practice • Improve</p>
                              </div>
                            </div>
                            <button
                              onClick={() => handleEndCall(false)}
                              className="shrink-0 px-2.5 py-1.5 rounded-full bg-[#ffe8f0] text-[#f43f5e] text-[10px] font-bold flex items-center gap-1"
                            >
                              <Flag className="w-3 h-3" />
                              End Practice
                            </button>
                          </div>

                          <div className="flex flex-col items-center pt-3 pb-2">
                            <div className="relative flex items-center justify-center">
                              <div className="absolute left-[-38px] flex items-end gap-[3px] h-8">
                                {[10, 18, 12, 22, 14].map((h, i) => (
                                  <div
                                    key={`l-${i}`}
                                    className="w-[3px] rounded-full bg-[#8bb4ff] animate-pulse"
                                    style={{ height: isMuted ? '4px' : `${h}px`, animationDelay: `${i * 120}ms` }}
                                  />
                                ))}
                              </div>
                              <div className="w-[86px] h-[86px] rounded-full bg-white p-[3px] shadow-[0_8px_20px_rgba(80,120,200,0.12)]">
                                <div className="w-full h-full rounded-full overflow-hidden bg-[#d6ecff]">
                                  <img src="/avatar-anand.svg" alt={partnerFirstName} className="w-full h-full object-cover" />
                                </div>
                              </div>
                              <div className="absolute right-[-38px] flex items-end gap-[3px] h-8">
                                {[14, 22, 12, 18, 10].map((h, i) => (
                                  <div
                                    key={`r-${i}`}
                                    className="w-[3px] rounded-full bg-[#8bb4ff] animate-pulse"
                                    style={{ height: isMuted ? '4px' : `${h}px`, animationDelay: `${i * 120}ms` }}
                                  />
                                ))}
                              </div>
                            </div>
                            <h3 className="text-[20px] font-extrabold text-[#1b2559] mt-2.5 leading-none">{partnerFirstName}</h3>
                            <p className="text-[11px] text-[#8b95b7] mt-1.5 font-medium">{partnerLocation}</p>
                            <span className="mt-2 px-3 py-1 rounded-full bg-[#e6f8ef] text-[#22c55e] text-[11px] font-bold flex items-center gap-1.5">
                              <span className="w-1.5 h-1.5 rounded-full bg-[#22c55e]" />
                              Connected
                            </span>
                            <p className="text-[22px] font-extrabold text-[#1b2559] mt-2.5 tracking-tight">{formatTimer(callSecs)}</p>
                            <p className="text-[11px] text-[#8b95b7] mt-0.5">Keep the conversation going!</p>
                            {!isProUser && (
                              <p className="text-[9px] text-[#8b95b7] mt-1">Free • {formatTimer(Math.max(0, 600 - callSecs))} left</p>
                            )}
                          </div>

                          <div className="rounded-[18px] bg-white/90 border border-white shadow-[0_8px_20px_rgba(80,120,200,0.06)] p-2.5">
                            <div className="flex items-center justify-between mb-2 px-0.5">
                              <div className="flex items-center gap-1.5">
                                <MessageSquare className="w-3.5 h-3.5 text-[#7b61ff]" />
                                <span className="text-[12px] font-extrabold text-[#1b2559]">Suggested Topics</span>
                              </div>
                              <button
                                onClick={() => setTopicSetIndex(prev => prev + 1)}
                                className="flex items-center gap-1 text-[10px] font-bold text-[#3d6ef5]"
                              >
                                <RefreshCw className="w-3 h-3" />
                                Change
                              </button>
                            </div>
                            <div className="grid grid-cols-3 gap-1.5">
                              {activeTopics.map(topic => {
                                const TopicIcon = topic.icon;
                                return (
                                  <div key={topic.title} className="rounded-[14px] bg-[#f7f9fd] border border-[#eef2f8] px-1.5 py-2 text-center">
                                    <div className="w-8 h-8 mx-auto rounded-xl bg-white flex items-center justify-center text-[#3d6ef5] mb-1">
                                      <TopicIcon className="w-4 h-4" />
                                    </div>
                                    <p className="text-[10px] font-extrabold text-[#1b2559] leading-tight">{topic.title}</p>
                                    <p className="text-[8px] text-[#8b95b7] mt-0.5 leading-tight">{topic.desc}</p>
                                  </div>
                                );
                              })}
                            </div>
                          </div>

                          <div className="flex items-end justify-between px-1 pt-3 pb-1">
                            <button onClick={() => setIsMuted(!isMuted)} className="flex flex-col items-center gap-1 w-[52px]">
                              <div className={`w-11 h-11 rounded-full flex items-center justify-center ${isMuted ? 'bg-[#ffe8ee] text-[#f43f5e]' : 'bg-[#eef3fb] text-[#5b6b8c]'}`}>
                                {isMuted ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
                              </div>
                              <span className="text-[10px] font-semibold text-[#8b95b7]">Mute</span>
                            </button>
                            <button onClick={() => setIsSpeaker(!isSpeaker)} className="flex flex-col items-center gap-1 w-[52px]">
                              <div className={`w-11 h-11 rounded-full flex items-center justify-center ${isSpeaker ? 'bg-[#e8f0ff] text-[#3d6ef5]' : 'bg-[#eef3fb] text-[#5b6b8c]'}`}>
                                {isSpeaker ? <Volume2 className="w-5 h-5" /> : <VolumeX className="w-5 h-5" />}
                              </div>
                              <span className="text-[10px] font-semibold text-[#8b95b7]">Speaker</span>
                            </button>
                            <button onClick={() => handleEndCall(false)} className="flex flex-col items-center gap-1 w-[64px]">
                              <div className="w-14 h-14 rounded-full bg-[#ff3b30] text-white flex items-center justify-center shadow-[0_8px_16px_rgba(255,59,48,0.35)]">
                                <PhoneOff className="w-6 h-6" />
                              </div>
                              <span className="text-[10px] font-bold text-[#1b2559]">End Call</span>
                            </button>
                            <button className="flex flex-col items-center gap-1 w-[52px]">
                              <div className="w-11 h-11 rounded-full bg-[#eef3fb] text-[#5b6b8c] flex items-center justify-center">
                                <MoreHorizontal className="w-5 h-5" />
                              </div>
                              <span className="text-[10px] font-semibold text-[#8b95b7]">More</span>
                            </button>
                            <button
                              onClick={() => {
                                const friend = friendsList[0];
                                if (friend) handleOpenChat(friend);
                              }}
                              className="flex flex-col items-center gap-1 w-[52px]"
                            >
                              <div className="w-11 h-11 rounded-full bg-[#eef3fb] text-[#5b6b8c] flex items-center justify-center">
                                <MessageSquare className="w-5 h-5" />
                              </div>
                              <span className="text-[10px] font-semibold text-[#8b95b7]">Chat</span>
                            </button>
                          </div>

                          <button className="mt-2 rounded-[16px] bg-[#f3f7ff] border border-[#e8eef8] px-3 py-2.5 flex items-center gap-2 text-left">
                            <div className="w-8 h-8 rounded-full bg-white flex items-center justify-center text-[#3d6ef5] shrink-0">
                              <Lightbulb className="w-4 h-4" />
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="text-[11px] font-extrabold text-[#1b2559]">Tip for Better Practice</p>
                              <p className="text-[9px] text-[#8b95b7] leading-snug">Try to speak in complete sentences and don't worry about mistakes!</p>
                            </div>
                            <ChevronRight className="w-4 h-4 text-[#c5cde0] shrink-0" />
                          </button>
                        </div>
                      )}

                      {simState === 'ENDED' && (
                        <div className="flex-1 flex flex-col items-center justify-center text-center px-5">
                          {isLimitReached ? (
                            <div className="w-full flex flex-col items-center space-y-3">
                              <div className="w-16 h-16 rounded-full bg-[#fff4d6] flex items-center justify-center text-[#f5a623]">
                                <Crown className="w-8 h-8" />
                              </div>
                              <h3 className="text-base font-extrabold text-[#1b2559]">10-Minute Free Call Ended</h3>
                              <p className="text-xs text-[#8b95b7] leading-relaxed max-w-[280px]">
                                Free users can speak for <strong className="text-[#1b2559]">up to 10 mins per call</strong>. Start your next free call instantly or unlock non-stop calls with VIP Pass!
                              </p>
                              <div className="w-full space-y-2 pt-2">
                                <button
                                  onClick={() => {
                                    setSimState('IDLE');
                                    handleStartSearch();
                                  }}
                                  className="w-full py-2.5 rounded-xl bg-[#3d6ef5] text-white text-xs font-bold"
                                >
                                  Start Next Free Call
                                </button>
                                <button
                                  onClick={() => {
                                    setSimState('IDLE');
                                    setCurrentAppTab('SUBSCRIPTION');
                                  }}
                                  className="w-full py-2.5 rounded-xl bg-white border border-[#e6edf8] text-[#1b2559] text-xs font-semibold flex items-center justify-center gap-1"
                                >
                                  <Crown className="w-3.5 h-3.5 text-[#f5a623]" />
                                  <span>Remove 10-Min Limit (₹100 / 5 Mo)</span>
                                </button>
                              </div>
                            </div>
                          ) : (
                            <div>
                              <div className="w-16 h-16 rounded-full bg-white border border-[#e6edf8] flex items-center justify-center mb-3 mx-auto">
                                <PhoneOff className="w-7 h-7 text-[#f43f5e]" />
                              </div>
                              <h3 className="text-base font-extrabold text-[#1b2559]">Call Ended</h3>
                              <p className="text-xs text-[#8b95b7] mt-1">Duration: {formatTimer(callSecs)} • Free Session</p>
                              <button
                                onClick={() => setSimState('IDLE')}
                                className="mt-4 px-5 py-2 rounded-full bg-[#3d6ef5] text-white text-xs font-bold"
                              >
                                Back Home
                              </button>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ) : (
                    /* App Screen Views (Home / Friends / Subscription / Profile) */
                    <div className="flex-1 flex flex-col overflow-hidden">
                      
                      {/* App Top Bar */}
                      <div className={`px-4 py-3 border-b flex items-center justify-between z-10 shrink-0 ${currentAppTab === 'PROFILE' || currentAppTab === 'SUBSCRIPTION' || currentAppTab === 'FRIENDS' || currentAppTab === 'HOME' ? 'hidden' : 'bg-[#0d1017] border-slate-800/80'}`}>
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => setCurrentAppTab('PROFILE')}
                            className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-900 border border-slate-800 text-xs font-semibold text-slate-200 hover:border-emerald-500/50 transition-colors"
                          >
                            {profileImage ? (
                              <img src={profileImage} alt="" className="w-3.5 h-3.5 rounded-full object-cover" />
                            ) : (
                              <span className="w-2 h-2 rounded-full bg-emerald-400" />
                            )}
                            <span className="truncate max-w-[100px]">{isLoggedIn ? userName : 'Profile'}</span>
                          </button>
                          
                          <div className="flex items-center gap-1 px-2 py-1 rounded-full bg-amber-500/10 text-amber-400 text-xs font-bold border border-amber-500/20">
                            <Flame className="w-3.5 h-3.5" />
                            <span>{userStreak}</span>
                          </div>
                        </div>

                        <button
                          onClick={() => setCurrentAppTab('SUBSCRIPTION')}
                          className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-gradient-to-r from-amber-500 to-amber-600 text-slate-950 text-xs font-bold shadow-sm"
                        >
                          <Crown className="w-3.5 h-3.5" />
                          <span>{isProUser ? 'PRO Active' : '5-Mo Plan'}</span>
                        </button>
                      </div>

                      {/* Screen Content Body */}
                      <div className={`flex-1 overflow-y-auto ${currentAppTab === 'PROFILE' || currentAppTab === 'SUBSCRIPTION' || currentAppTab === 'FRIENDS' || currentAppTab === 'HOME' ? 'p-0 space-y-0' : 'p-4 space-y-4'}`}>
                        
                        {/* TAB 1: HOME SCREEN */}
                        {currentAppTab === 'HOME' && (
                          <div className="px-3.5 pt-3 pb-3 space-y-3 bg-[#eef3fb] min-h-full">
                            <div className="flex items-start justify-between">
                              <div>
                                <h2 className="text-[22px] leading-none font-extrabold text-[#1b2559] tracking-tight">Hello, {isLoggedIn ? userName.split(' ')[0] : 'Learner'}!</h2>
                                <p className="text-[11px] text-[#8b95b7] mt-1.5 font-medium">Let's speak, practice and grow together</p>
                              </div>
                              <div className="flex items-center gap-2">
                                <div className="h-9 px-2.5 rounded-full bg-white shadow-[0_4px_14px_rgba(59,99,237,0.08)] flex items-center gap-1 text-[12px] font-extrabold text-[#1b2559]">
                                  <Flame className="w-3.5 h-3.5 text-[#f97316] fill-[#f97316]" />
                                  {userStreak || 7}
                                </div>
                                <button
                                  onClick={() => setCurrentAppTab('PROFILE')}
                                  className="w-9 h-9 rounded-full bg-white shadow-[0_4px_14px_rgba(59,99,237,0.08)] flex items-center justify-center text-[#5b6b8c] overflow-hidden"
                                >
                                  {profileImage ? (
                                    <img src={profileImage} alt="" className="w-full h-full object-cover" />
                                  ) : (
                                    <User className="w-4 h-4" />
                                  )}
                                </button>
                              </div>
                            </div>

                            <div className="rounded-[22px] bg-gradient-to-r from-[#eaf3ff] to-[#f4f8ff] border border-white shadow-[0_8px_24px_rgba(80,120,200,0.08)] p-3.5 relative overflow-hidden min-h-[168px]">
                              <div className="pr-[118px]">
                                <p className="text-[9px] font-bold tracking-[0.14em] text-[#8b95b7] uppercase">Speak Confidently</p>
                                <h3 className="text-[18px] font-extrabold text-[#1b2559] leading-tight mt-1">Practice Speaking Without Fear</h3>
                                <p className="text-[11px] text-[#8b95b7] mt-1.5 leading-snug">Improve your English speaking skills with real conversations and fun practice sessions.</p>
                                <button
                                  onClick={handleStartSearch}
                                  className="mt-3 px-4 py-2 rounded-full bg-[#3d6ef5] text-white text-[12px] font-bold inline-flex items-center gap-1.5 shadow-[0_8px_16px_rgba(61,110,245,0.28)]"
                                >
                                  Start Speaking
                                  <ArrowRight className="w-3.5 h-3.5" />
                                </button>
                              </div>
                              <div className="absolute right-1 bottom-0 w-[120px] flex flex-col items-center">
                                <span className="absolute top-2 right-2 text-[9px] font-extrabold italic text-[#3d6ef5] leading-tight text-right">Better English<br />Brighter You!</span>
                                <img src="/avatar-anand.svg" alt="" className="w-[108px] h-[108px] object-contain mt-6" />
                              </div>
                            </div>

                            <div className="grid grid-cols-3 gap-2">
                              <button onClick={handleStartSearch} className="flex flex-col items-center text-center">
                                <div className="w-12 h-12 rounded-2xl bg-[#ffe4ee] flex items-center justify-center text-[#f43f5e]">
                                  <Users className="w-5 h-5" />
                                </div>
                                <p className="text-[11px] font-extrabold text-[#1b2559] mt-1.5 leading-tight">Real Situations</p>
                                <p className="text-[9px] text-[#8b95b7] mt-0.5">Daily life topics</p>
                              </button>
                              <button onClick={handleStartSearch} className="flex flex-col items-center text-center">
                                <div className="w-12 h-12 rounded-2xl bg-[#fff4d6] flex items-center justify-center text-[#f5a623]">
                                  <BookOpen className="w-5 h-5" />
                                </div>
                                <p className="text-[11px] font-extrabold text-[#1b2559] mt-1.5 leading-tight">Learn Phrases</p>
                                <p className="text-[9px] text-[#8b95b7] mt-0.5">Useful sentences</p>
                              </button>
                              <button onClick={() => setCurrentAppTab('PROFILE')} className="flex flex-col items-center text-center">
                                <div className="w-12 h-12 rounded-2xl bg-[#ece8ff] flex items-center justify-center text-[#7b61ff]">
                                  <BarChart3 className="w-5 h-5" />
                                </div>
                                <p className="text-[11px] font-extrabold text-[#1b2559] mt-1.5 leading-tight">Track Progress</p>
                                <p className="text-[9px] text-[#8b95b7] mt-0.5">See your growth</p>
                              </button>
                            </div>

                            <div>
                              <div className="flex items-center justify-between mb-2">
                                <h4 className="text-[15px] font-extrabold text-[#1b2559]">Practice by Category</h4>
                                <button className="text-[11px] font-bold text-[#3d6ef5] flex items-center gap-0.5">
                                  See All <ChevronRight className="w-3.5 h-3.5" />
                                </button>
                              </div>
                              <div className="grid grid-cols-2 gap-2">
                                <button onClick={handleStartSearch} className="rounded-[18px] bg-[#eaf3ff] px-3 py-3 text-left">
                                  <Briefcase className="w-5 h-5 text-[#3d6ef5]" />
                                  <p className="text-[12px] font-extrabold text-[#1b2559] mt-2 leading-tight">Work & Career</p>
                                  <p className="text-[10px] text-[#8b95b7] mt-0.5">12 lessons</p>
                                </button>
                                <button onClick={handleStartSearch} className="rounded-[18px] bg-[#e6f8ef] px-3 py-3 text-left">
                                  <Plane className="w-5 h-5 text-[#22c55e]" />
                                  <p className="text-[12px] font-extrabold text-[#1b2559] mt-2 leading-tight">Travel & Tourism</p>
                                  <p className="text-[10px] text-[#8b95b7] mt-0.5">10 lessons</p>
                                </button>
                                <button onClick={handleStartSearch} className="rounded-[18px] bg-[#fff0f4] px-3 py-3 text-left">
                                  <Coffee className="w-5 h-5 text-[#f43f5e]" />
                                  <p className="text-[12px] font-extrabold text-[#1b2559] mt-2 leading-tight">Daily Life</p>
                                  <p className="text-[10px] text-[#8b95b7] mt-0.5">15 lessons</p>
                                </button>
                                <button onClick={handleStartSearch} className="rounded-[18px] bg-[#fff8e8] px-3 py-3 text-left">
                                  <GraduationCap className="w-5 h-5 text-[#f5a623]" />
                                  <p className="text-[12px] font-extrabold text-[#1b2559] mt-2 leading-tight">Education</p>
                                  <p className="text-[10px] text-[#8b95b7] mt-0.5">10 lessons</p>
                                </button>
                              </div>
                            </div>

                            <div className="rounded-[18px] bg-[#f3f0ff] px-3.5 py-3 flex items-center gap-3">
                              <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center text-[#7b61ff] shrink-0">
                                <Target className="w-5 h-5" />
                              </div>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-start justify-between gap-2">
                                  <div>
                                    <p className="text-[13px] font-extrabold text-[#1b2559] leading-tight">Your Daily Goal</p>
                                    <p className="text-[10px] text-[#8b95b7] mt-0.5">Practice for 10 minutes daily</p>
                                  </div>
                                  <button
                                    onClick={handleStartSearch}
                                    className="shrink-0 px-2.5 py-1 rounded-full bg-white text-[#7b61ff] text-[10px] font-bold"
                                  >
                                    Keep Going
                                  </button>
                                </div>
                                <div className="mt-2 flex items-center gap-2">
                                  <div className="flex-1 h-[6px] rounded-full bg-white overflow-hidden">
                                    <div className="h-full w-[30%] rounded-full bg-[#3d6ef5]" />
                                  </div>
                                  <span className="text-[9px] font-semibold text-[#8b95b7]">3/10 min</span>
                                </div>
                              </div>
                            </div>
                          </div>
                        )}

                        {/* TAB 2: FRIENDS SCREEN */}
                        {currentAppTab === 'FRIENDS' && (
                          <div className="px-3.5 pt-3 pb-3 space-y-3 bg-[#eef3fb] min-h-full">
                            <div className="flex items-start justify-between">
                              <div>
                                <h2 className="text-[26px] leading-none font-extrabold text-[#1b2559] tracking-tight">Friends</h2>
                                <p className="text-[12px] text-[#8b95b7] mt-1.5 font-medium">Meet, chat and practice together</p>
                              </div>
                              <button
                                onClick={() => setFriendsPane('search')}
                                className="w-10 h-10 rounded-full bg-white shadow-[0_4px_14px_rgba(59,99,237,0.08)] flex items-center justify-center text-[#3d6ef5]"
                              >
                                <UserPlus className="w-[18px] h-[18px]" strokeWidth={2.2} />
                              </button>
                            </div>

                            <div className="rounded-[22px] bg-gradient-to-r from-[#eaf3ff] to-[#f4f8ff] border border-white shadow-[0_8px_24px_rgba(80,120,200,0.08)] p-3.5 relative overflow-hidden min-h-[132px]">
                              <div className="pr-[128px]">
                                <h3 className="text-[16px] font-extrabold text-[#1b2559] leading-tight">Find Friends<br />Around the World</h3>
                                <p className="text-[11px] text-[#8b95b7] mt-1.5 leading-snug">Chat, call and improve your English together.</p>
                                <button
                                  onClick={() => setFriendsPane('search')}
                                  className="mt-3 px-3.5 py-1.5 rounded-full bg-[#3d6ef5] text-white text-[11px] font-bold inline-flex items-center gap-1"
                                >
                                  Find Friends
                                  <ArrowRight className="w-3 h-3" />
                                </button>
                              </div>
                              <div className="absolute right-2 bottom-1 flex items-end">
                                <div className="absolute -top-2 left-2 px-1.5 py-0.5 rounded-lg bg-white text-[8px] font-bold text-[#3d6ef5] shadow-sm">Make Friends</div>
                                <img src="/avatar-anand.svg" alt="" className="w-[64px] h-[64px] object-contain" />
                                <img src="/avatar-girl.svg" alt="" className="w-[64px] h-[64px] object-contain -ml-2" />
                                <div className="absolute -top-1 right-1 px-1.5 py-0.5 rounded-lg bg-white text-[8px] font-bold text-[#7b61ff] shadow-sm">Practice Together</div>
                              </div>
                            </div>

                            <div className="rounded-full bg-[#f3f7ff] p-1 flex items-center">
                              <button
                                onClick={() => setFriendsPane('all')}
                                className={`flex-1 h-8 rounded-full text-[11px] font-bold flex items-center justify-center gap-1 ${friendsPane === 'all' ? 'bg-white text-[#3d6ef5] shadow-sm' : 'text-[#8b95b7]'}`}
                              >
                                <Users className="w-3.5 h-3.5" />
                                All Friends
                              </button>
                              <button
                                onClick={() => setFriendsPane('requests')}
                                className={`flex-1 h-8 rounded-full text-[11px] font-bold flex items-center justify-center gap-1 ${friendsPane === 'requests' ? 'bg-white text-[#3d6ef5] shadow-sm' : 'text-[#8b95b7]'}`}
                              >
                                <UserPlus className="w-3.5 h-3.5" />
                                Requests
                              </button>
                              <button
                                onClick={() => setFriendsPane('search')}
                                className={`flex-1 h-8 rounded-full text-[11px] font-bold flex items-center justify-center gap-1 ${friendsPane === 'search' ? 'bg-white text-[#3d6ef5] shadow-sm' : 'text-[#8b95b7]'}`}
                              >
                                <Search className="w-3.5 h-3.5" />
                                Search
                              </button>
                            </div>

                            <form onSubmit={handleAddFriend} className="flex items-center gap-2">
                              <div className="flex-1 h-10 rounded-full bg-white border border-[#e6edf8] px-3 flex items-center gap-2">
                                <Search className="w-3.5 h-3.5 text-[#8b95b7]" />
                                <input
                                  type="text"
                                  placeholder="Search by name or country..."
                                  value={friendsPane === 'search' ? newFriendIdInput : friendSearchQuery}
                                  onChange={e => {
                                    if (friendsPane === 'search') setNewFriendIdInput(e.target.value);
                                    else setFriendSearchQuery(e.target.value);
                                  }}
                                  className="flex-1 bg-transparent text-[11px] text-[#1b2559] placeholder-[#8b95b7] outline-none"
                                />
                              </div>
                              <button type="button" className="w-10 h-10 rounded-2xl bg-white border border-[#e6edf8] flex items-center justify-center text-[#5b6b8c]">
                                <SlidersHorizontal className="w-4 h-4" />
                              </button>
                            </form>

                            {friendAddSuccess && (
                              <div className="px-3 py-2 rounded-xl bg-[#e6f8ef] text-[#16a34a] text-[11px] font-semibold flex items-center gap-1.5">
                                <CheckCircle2 className="w-4 h-4" />
                                Friend added to your practice circle!
                              </div>
                            )}

                            {friendsPane === 'requests' ? (
                              <div className="rounded-[18px] bg-white px-4 py-8 text-center">
                                <UserPlus className="w-8 h-8 text-[#c5cde0] mx-auto" />
                                <p className="text-[13px] font-extrabold text-[#1b2559] mt-2">No requests yet</p>
                                <p className="text-[11px] text-[#8b95b7] mt-1">New friend requests will show up here.</p>
                              </div>
                            ) : (
                              <>
                                <div>
                                  <div className="flex items-center justify-between mb-2">
                                    <h4 className="text-[14px] font-extrabold text-[#1b2559]">Suggested Friends</h4>
                                    <button className="text-[11px] font-bold text-[#3d6ef5] flex items-center gap-0.5">
                                      See All <ChevronRight className="w-3.5 h-3.5" />
                                    </button>
                                  </div>
                                  <div className="flex gap-2 overflow-x-auto pb-1">
                                    {suggestedFriends.map(friend => (
                                      <div key={friend.id} className={`min-w-[86px] rounded-[18px] ${friend.tone || 'bg-[#eaf3ff]'} px-2 py-2.5 flex flex-col items-center text-center`}>
                                        <div className="relative">
                                          <div className={`w-12 h-12 rounded-full overflow-hidden ${friend.avatarColor}`}>
                                            <img src={friend.name === 'Sara' || friend.name === 'Priya' ? '/avatar-girl.svg' : '/avatar-anand.svg'} alt="" className="w-full h-full object-cover" />
                                          </div>
                                          {friend.status === 'Online' && (
                                            <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-[#22c55e] border-2 border-white" />
                                          )}
                                        </div>
                                        <p className="text-[11px] font-extrabold text-[#1b2559] mt-1.5 leading-tight">{friend.name}</p>
                                        <p className="text-[9px] text-[#8b95b7] mt-0.5">{friend.location}</p>
                                        <button
                                          onClick={() => {
                                            setFriendsList(prev => prev.some(item => item.id === friend.id) ? prev : [{ ...friend, subtitle: 'Online' }, ...prev]);
                                            setSuggestedFriends(prev => prev.filter(item => item.id !== friend.id));
                                            setFriendAddSuccess(true);
                                            setTimeout(() => setFriendAddSuccess(false), 3000);
                                          }}
                                          className="mt-1.5 w-full py-1 rounded-full bg-white text-[#3d6ef5] text-[10px] font-bold"
                                        >
                                          Add
                                        </button>
                                      </div>
                                    ))}
                                  </div>
                                </div>

                                <div>
                                  <h4 className="text-[14px] font-extrabold text-[#1b2559] mb-1">My Friends</h4>
                                  <div className="rounded-[18px] bg-white overflow-hidden">
                                    {friendsList
                                      .filter(friend => {
                                        const q = (friendsPane === 'search' ? newFriendIdInput : friendSearchQuery).trim().toLowerCase();
                                        if (!q) return true;
                                        return friend.name.toLowerCase().includes(q) || friend.location.toLowerCase().includes(q) || (friend.country || '').toLowerCase().includes(q);
                                      })
                                      .map((friend, index, list) => (
                                      <div key={friend.id} className={`px-3 py-2.5 flex items-center gap-2.5 ${index < list.length - 1 ? 'border-b border-[#eef2f8]' : ''}`}>
                                        <div className="relative shrink-0">
                                          <div className={`w-10 h-10 rounded-full overflow-hidden ${friend.avatarColor}`}>
                                            <img src={friend.name === 'Emma' || friend.name === 'Aisha' || friend.name === 'Sara' || friend.name === 'Priya' ? '/avatar-girl.svg' : '/avatar-anand.svg'} alt="" className="w-full h-full object-cover" />
                                          </div>
                                          <span className={`absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full border-2 border-white ${friend.status === 'Offline' ? 'bg-[#c5cde0]' : 'bg-[#22c55e]'}`} />
                                        </div>
                                        <div className="flex-1 min-w-0">
                                          <p className="text-[13px] font-extrabold text-[#1b2559] leading-tight">{friend.name}</p>
                                          <p className="text-[10px] text-[#8b95b7] mt-0.5 truncate">{friend.subtitle || `${friend.level} • ${friend.location}`}</p>
                                        </div>
                                        <button
                                          onClick={() => handleOpenChat(friend)}
                                          className="flex flex-col items-center gap-0.5"
                                        >
                                          <div className="w-8 h-8 rounded-full bg-[#e8f0ff] text-[#3d6ef5] flex items-center justify-center">
                                            <MessageSquare className="w-3.5 h-3.5" />
                                          </div>
                                          <span className="text-[8px] font-semibold text-[#8b95b7]">Chat</span>
                                        </button>
                                        <button
                                          onClick={() => handleStartDirectCall(friend)}
                                          className="flex flex-col items-center gap-0.5"
                                        >
                                          <div className="w-8 h-8 rounded-full bg-[#e8f0ff] text-[#3d6ef5] flex items-center justify-center">
                                            <Phone className="w-3.5 h-3.5" />
                                          </div>
                                          <span className="text-[8px] font-semibold text-[#8b95b7]">Call</span>
                                        </button>
                                        <button className="text-[#c5cde0] pl-0.5">
                                          <MoreHorizontal className="w-4 h-4" />
                                        </button>
                                      </div>
                                    ))}
                                  </div>
                                </div>
                              </>
                            )}
                          </div>
                        )}

                        {/* TAB 3: SUBSCRIPTION SCREEN */}
                        {currentAppTab === 'SUBSCRIPTION' && (
                          <div className="px-4 pt-2 pb-4 space-y-3 bg-[#eef3fb] min-h-full">
                            <div className="flex items-start gap-2">
                              <button
                                onClick={() => setCurrentAppTab('HOME')}
                                className="mt-0.5 w-8 h-8 rounded-full flex items-center justify-center text-[#1b2559]"
                              >
                                <ChevronLeft className="w-5 h-5" strokeWidth={2.4} />
                              </button>
                              <div>
                                <h2 className="text-[22px] leading-none font-extrabold text-[#1b2559] tracking-tight">Get Premium</h2>
                                <p className="text-[11px] text-[#8b95b7] mt-1.5 font-medium">Unlock more. Speak without limits.</p>
                              </div>
                            </div>

                            {isProUser && (
                              <div className="rounded-[18px] bg-white border border-[#d7e4ff] px-3.5 py-2.5 flex items-center justify-between">
                                <div>
                                  <p className="text-[12px] font-extrabold text-[#1b2559]">VIP PRO Active</p>
                                  <p className="text-[10px] text-[#8b95b7]">Valid until {playExpiryDate}</p>
                                </div>
                                <span className="text-[10px] font-bold text-[#3d6ef5]">5-Month Pass</span>
                              </div>
                            )}

                            <div className="rounded-[22px] bg-gradient-to-br from-[#eaf3ff] via-[#f5f8ff] to-[#eef4ff] border border-white shadow-[0_8px_24px_rgba(80,120,200,0.08)] p-3.5 relative overflow-hidden">
                              <div className="absolute right-2 top-3 w-[118px] flex flex-col items-center">
                                <Crown className="w-7 h-7 text-[#f5c518] fill-[#f5c518] mb-1 drop-shadow-[0_3px_6px_rgba(245,197,24,0.45)]" />
                                <img src={profileImage || '/avatar-anand.svg'} alt="" className="w-[72px] h-[72px] object-contain" />
                                <div className="mt-1 px-2 py-1 rounded-xl bg-white/90 shadow-sm text-center">
                                  <p className="text-[9px] font-extrabold text-[#1b2559] leading-tight">Premium Learner</p>
                                  <p className="text-[8px] text-[#8b95b7]">Speak • Connect • Grow</p>
                                </div>
                              </div>
                              <div className="pr-[118px]">
                                <h3 className="text-[20px] font-extrabold text-[#1b2559] leading-tight">Speak Better<br />Go Further</h3>
                                <p className="text-[11px] text-[#8b95b7] mt-1.5 leading-snug">Get Premium and enjoy unlimited practice, calls, and advanced features.</p>
                              </div>
                              <div className="mt-4 grid grid-cols-4 gap-1">
                                <div className="flex flex-col items-center text-center">
                                  <div className="w-9 h-9 rounded-full bg-[#e8f0ff] flex items-center justify-center text-[#3d6ef5]">
                                    <Mic className="w-4 h-4" />
                                  </div>
                                  <span className="text-[9px] font-semibold text-[#5b6b8c] mt-1 leading-tight">Unlimited Speaking</span>
                                </div>
                                <div className="flex flex-col items-center text-center">
                                  <div className="w-9 h-9 rounded-full bg-[#e6f8ef] flex items-center justify-center text-[#22c55e]">
                                    <Video className="w-4 h-4" />
                                  </div>
                                  <span className="text-[9px] font-semibold text-[#5b6b8c] mt-1 leading-tight">Call with Friends</span>
                                </div>
                                <div className="flex flex-col items-center text-center">
                                  <div className="w-9 h-9 rounded-full bg-[#ece8ff] flex items-center justify-center text-[#7b61ff]">
                                    <BarChart3 className="w-4 h-4" />
                                  </div>
                                  <span className="text-[9px] font-semibold text-[#5b6b8c] mt-1 leading-tight">Detailed Feedback</span>
                                </div>
                                <div className="flex flex-col items-center text-center">
                                  <div className="w-9 h-9 rounded-full bg-[#fff4d6] flex items-center justify-center text-[#f5a623]">
                                    <Crown className="w-4 h-4" />
                                  </div>
                                  <span className="text-[9px] font-semibold text-[#5b6b8c] mt-1 leading-tight">Premium Topics</span>
                                </div>
                              </div>
                            </div>

                            <h4 className="text-[16px] font-extrabold text-[#1b2559] pt-0.5">Choose Your Plan</h4>
                            <div className="grid grid-cols-3 gap-1.5 items-stretch">
                              <div className="rounded-[16px] bg-white border border-[#e6edf8] px-2 py-3 flex flex-col items-center text-center">
                                <p className="text-[11px] font-bold text-[#1b2559]">1 Month</p>
                                <p className="text-[18px] font-extrabold text-[#1b2559] mt-1">₹49</p>
                                <p className="text-[9px] text-[#8b95b7] mt-0.5">₹49 per month</p>
                                <button
                                  onClick={handleOpenPlayBilling}
                                  className="mt-auto w-full mt-3 py-1.5 rounded-full bg-[#eef4ff] text-[#3d6ef5] text-[10px] font-bold"
                                >
                                  Subscribe
                                </button>
                              </div>
                              <div className="relative rounded-[16px] bg-gradient-to-b from-[#eef5ff] to-white border-2 border-[#3d6ef5] px-2 pt-4 pb-3 flex flex-col items-center text-center shadow-[0_8px_18px_rgba(61,110,245,0.12)]">
                                <span className="absolute -top-2.5 left-1/2 -translate-x-1/2 px-2 py-0.5 rounded-full bg-[#3d6ef5] text-white text-[9px] font-bold whitespace-nowrap">Best Value</span>
                                <p className="text-[11px] font-bold text-[#1b2559]">5 Months</p>
                                <p className="text-[18px] font-extrabold text-[#3d6ef5] mt-1">₹100</p>
                                <p className="text-[9px] text-[#8b95b7] mt-0.5">₹20 per month</p>
                                <span className="mt-1 px-1.5 py-0.5 rounded-full bg-[#e6f8ef] text-[#22c55e] text-[8px] font-bold">Save 59%</span>
                                <button
                                  onClick={handleOpenPlayBilling}
                                  className="mt-2 w-full py-1.5 rounded-full bg-[#3d6ef5] text-white text-[10px] font-bold shadow-sm"
                                >
                                  Subscribe
                                </button>
                              </div>
                              <div className="rounded-[16px] bg-white border border-[#e6edf8] px-2 py-3 flex flex-col items-center text-center opacity-90">
                                <p className="text-[11px] font-bold text-[#1b2559]">12 Months</p>
                                <p className="text-[13px] font-extrabold text-[#8b95b7] mt-2 leading-tight">Will soon</p>
                                <p className="text-[9px] text-[#8b95b7] mt-1">Coming soon</p>
                                <button
                                  disabled
                                  className="mt-auto w-full mt-3 py-1.5 rounded-full bg-[#eef4ff] text-[#a8b4d0] text-[10px] font-bold cursor-not-allowed"
                                >
                                  Will soon
                                </button>
                              </div>
                            </div>

                            <button
                              onClick={handleOpenPlayBilling}
                              className="w-full py-3 rounded-[16px] bg-[#22c55e] hover:bg-[#16a34a] text-white text-[13px] font-extrabold flex items-center justify-center gap-2 shadow-[0_8px_18px_rgba(34,197,94,0.28)]"
                            >
                              <Play className="w-4 h-4 fill-white" />
                              Subscribe with Google Play
                              <ChevronRight className="w-4 h-4 ml-auto" />
                            </button>

                            <div className="flex items-center justify-between px-1 text-[9px] font-semibold text-[#8b95b7]">
                              <span className="flex items-center gap-1"><ShieldCheck className="w-3 h-3" /> Secure Payment</span>
                              <span className="flex items-center gap-1"><Lock className="w-3 h-3" /> Cancel Anytime</span>
                              <span className="flex items-center gap-1"><Play className="w-3 h-3" /> Trusted by Learners</span>
                            </div>

                            <div className="rounded-[18px] bg-white border border-[#eee7ff] px-3 py-2.5 flex items-center gap-3">
                              <div className="w-10 h-10 rounded-xl bg-[#fff0f3] flex items-center justify-center text-[#f43f5e] shrink-0">
                                <Gift className="w-5 h-5" />
                              </div>
                              <div className="flex-1 min-w-0">
                                <p className="text-[12px] font-extrabold text-[#1b2559]">Special Offer</p>
                                <p className="text-[10px] text-[#8b95b7] leading-snug">Get 5 months Premium at just ₹100! Limited time offer.</p>
                              </div>
                              <div className="text-right shrink-0">
                                <p className="text-[14px] font-extrabold text-[#7b61ff]">₹100</p>
                                <p className="text-[9px] text-[#7b61ff] font-semibold">for 5 months</p>
                              </div>
                            </div>
                          </div>
                        )}

                        {/* TAB 4: PROFILE SCREEN */}
                        {currentAppTab === 'PROFILE' && (
                          <div className="px-4 pt-3 pb-3 space-y-3 bg-[#eef3fb] min-h-full">
                            <input
                              type="file"
                              ref={fileInputRef}
                              accept="image/*"
                              onChange={handleProfileImageUpload}
                              className="hidden"
                              id="gallery-photo-input"
                            />

                            <div className="flex items-start justify-between">
                              <div>
                                <h2 className="text-[26px] leading-none font-extrabold text-[#1b2559] tracking-tight">Profile</h2>
                                <p className="text-[12px] text-[#8b95b7] mt-1.5 font-medium">Keep learning, keep growing!</p>
                              </div>
                              <button
                                onClick={() => setShowAuthModal(true)}
                                className="w-10 h-10 rounded-full bg-white shadow-[0_4px_14px_rgba(59,99,237,0.08)] flex items-center justify-center text-[#3d4a7a]"
                              >
                                <Settings className="w-[18px] h-[18px]" strokeWidth={2.2} />
                              </button>
                            </div>

                            <div className="rounded-[22px] bg-gradient-to-br from-[#eaf3ff] via-[#f4f8ff] to-[#eef4ff] border border-white/80 shadow-[0_8px_24px_rgba(80,120,200,0.08)] p-3.5">
                              <div className="flex items-center gap-3">
                                <div
                                  onClick={() => fileInputRef.current?.click()}
                                  className="relative shrink-0 cursor-pointer"
                                >
                                  <div className="w-[72px] h-[72px] rounded-full bg-white p-[3px] shadow-sm">
                                    <div className="w-full h-full rounded-full overflow-hidden bg-[#d6ecff]">
                                      <img
                                        src={profileImage || '/avatar-anand.svg'}
                                        alt={userName}
                                        className="w-full h-full object-cover"
                                      />
                                    </div>
                                  </div>
                                  <div className="absolute -bottom-0.5 -right-0.5 w-6 h-6 rounded-full bg-white border border-[#e6edf8] flex items-center justify-center text-[#5b6b8c] shadow-sm">
                                    <Camera className="w-3 h-3" />
                                  </div>
                                </div>

                                <div className="flex-1 min-w-0">
                                  <div className="flex items-start justify-between gap-2">
                                    <div className="min-w-0">
                                      <h3 className="text-[18px] font-extrabold text-[#1b2559] leading-tight truncate">
                                        {isLoggedIn ? userName : 'Anand'}
                                      </h3>
                                      <p className="text-[12px] text-[#8b95b7] mt-0.5">{selectedEnglishLevel === 'Intermediate' && !isLoggedIn ? 'Beginner' : selectedEnglishLevel}</p>
                                    </div>
                                    <button
                                      onClick={() => setShowAuthModal(true)}
                                      className="shrink-0 px-3 py-1.5 rounded-full bg-white text-[#4d7cff] text-[11px] font-bold shadow-[0_2px_8px_rgba(77,124,255,0.12)]"
                                    >
                                      Edit Profile
                                    </button>
                                  </div>
                                  <div className="mt-2 inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-[#e8f0ff] text-[#3d6ef5] text-[11px] font-bold">
                                    <Star className="w-3 h-3 fill-[#f5c518] text-[#f5c518]" />
                                    Keep Going
                                  </div>
                                </div>
                              </div>

                              <div className="mt-3.5 rounded-[16px] bg-white/90 border border-white px-2 py-2.5 grid grid-cols-3">
                                <div className="flex flex-col items-center justify-center">
                                  <div className="flex items-center gap-1.5">
                                    <Flame className="w-4 h-4 text-emerald-500 fill-emerald-500" />
                                    <span className="text-[16px] font-extrabold text-[#1b2559]">{userStreak || 5}</span>
                                  </div>
                                  <span className="text-[10px] text-[#8b95b7] mt-0.5">Day Streak</span>
                                </div>
                                <div className="flex flex-col items-center justify-center border-x border-[#eef2f8]">
                                  <div className="flex items-center gap-1.5">
                                    <BarChart3 className="w-4 h-4 text-[#7b61ff]" />
                                    <span className="text-[16px] font-extrabold text-[#1b2559]">12</span>
                                  </div>
                                  <span className="text-[10px] text-[#8b95b7] mt-0.5">Practice Sessions</span>
                                </div>
                                <div className="flex flex-col items-center justify-center">
                                  <div className="flex items-center gap-1.5">
                                    <Clock className="w-4 h-4 text-[#f5a623] fill-[#f5a623]" />
                                    <span className="text-[16px] font-extrabold text-[#1b2559]">30</span>
                                  </div>
                                  <span className="text-[10px] text-[#8b95b7] mt-0.5">Minutes Learned</span>
                                </div>
                              </div>
                            </div>

                            <div className="rounded-[22px] bg-gradient-to-r from-[#f3f7ff] to-[#eaf2ff] border border-white shadow-[0_8px_24px_rgba(80,120,200,0.08)] px-4 py-3.5 relative overflow-hidden">
                              <div className="pr-16">
                                <div className="flex items-center justify-between">
                                  <h4 className="text-[13px] font-extrabold text-[#1b2559]">Your Learning Progress</h4>
                                  <span className="text-[11px] font-bold text-[#3d6ef5]">Level 1</span>
                                </div>
                                <div className="mt-2.5 flex items-center gap-2">
                                  <div className="flex-1 h-[8px] rounded-full bg-[#dfe6f2] overflow-hidden">
                                    <div className="h-full w-[30%] rounded-full bg-[#3b82f6]" />
                                  </div>
                                  <span className="text-[11px] font-semibold text-[#8b95b7]">30%</span>
                                </div>
                                <p className="text-[10px] text-[#8b95b7] mt-2 leading-snug">Practice a little every day to reach Level 2!</p>
                              </div>
                              <div className="absolute right-3 top-1/2 -translate-y-1/2 w-14 h-14 flex items-center justify-center">
                                <Trophy className="w-11 h-11 text-[#f5c518] fill-[#f5c518] drop-shadow-[0_4px_8px_rgba(245,197,24,0.45)]" />
                              </div>
                            </div>

                            <div className="rounded-[22px] bg-white shadow-[0_8px_24px_rgba(80,120,200,0.08)] overflow-hidden">
                              <button
                                onClick={() => setShowAuthModal(true)}
                                className="w-full px-3.5 py-3 flex items-center gap-3 text-left"
                              >
                                <div className="w-10 h-10 rounded-2xl bg-[#ece8ff] flex items-center justify-center text-[#7b61ff] shrink-0">
                                  <User className="w-[18px] h-[18px]" />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className="text-[13px] font-extrabold text-[#1b2559]">My Profile</p>
                                  <p className="text-[11px] text-[#8b95b7]">View and update your information</p>
                                </div>
                                <ChevronRight className="w-4 h-4 text-[#c5cde0]" />
                              </button>
                              <div className="h-px bg-[#eef2f8] mx-3.5" />
                              <button
                                onClick={() => setCurrentAppTab('SUBSCRIPTION')}
                                className="w-full px-3.5 py-3 flex items-center gap-3 text-left"
                              >
                                <div className="w-10 h-10 rounded-2xl bg-[#e6f8ef] flex items-center justify-center text-[#22c55e] shrink-0">
                                  <BarChart3 className="w-[18px] h-[18px]" />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className="text-[13px] font-extrabold text-[#1b2559]">My Progress</p>
                                  <p className="text-[11px] text-[#8b95b7]">See your learning statistics</p>
                                </div>
                                <ChevronRight className="w-4 h-4 text-[#c5cde0]" />
                              </button>
                              <div className="h-px bg-[#eef2f8] mx-3.5" />
                              <button
                                onClick={() => setShowAuthModal(true)}
                                className="w-full px-3.5 py-3 flex items-center gap-3 text-left"
                              >
                                <div className="w-10 h-10 rounded-2xl bg-[#ffe8ee] flex items-center justify-center text-[#f43f5e] shrink-0">
                                  <Bell className="w-[18px] h-[18px]" />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className="text-[13px] font-extrabold text-[#1b2559]">Notifications</p>
                                  <p className="text-[11px] text-[#8b95b7]">Manage your notifications</p>
                                </div>
                                <ChevronRight className="w-4 h-4 text-[#c5cde0]" />
                              </button>
                              <div className="h-px bg-[#eef2f8] mx-3.5" />
                              <button
                                onClick={() => setShowPrivacyScreen(true)}
                                className="w-full px-3.5 py-3 flex items-center gap-3 text-left"
                              >
                                <div className="w-10 h-10 rounded-2xl bg-[#e8f0ff] flex items-center justify-center text-[#3d6ef5] shrink-0">
                                  <HelpCircle className="w-[18px] h-[18px]" />
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className="text-[13px] font-extrabold text-[#1b2559]">Help & Support</p>
                                  <p className="text-[11px] text-[#8b95b7]">Get help and find answers</p>
                                </div>
                                <ChevronRight className="w-4 h-4 text-[#c5cde0]" />
                              </button>
                            </div>
                          </div>
                        )}
                      </div>

                      {/* Bottom Navigation Bar */}
                      {currentAppTab === 'SUBSCRIPTION' ? null : (currentAppTab === 'PROFILE' || currentAppTab === 'FRIENDS' || currentAppTab === 'HOME') ? (
                        <div className="h-[62px] bg-white border-t border-[#eef2f8] px-2 flex items-center justify-around shrink-0 z-10">
                          <button
                            onClick={() => setCurrentAppTab('HOME')}
                            className={`flex flex-col items-center gap-0.5 text-[10px] ${currentAppTab === 'HOME' ? 'font-bold text-[#3d6ef5]' : 'font-semibold text-[#9aa3bb]'}`}
                          >
                            <Home className="w-[18px] h-[18px]" strokeWidth={2.4} fill={currentAppTab === 'HOME' ? 'currentColor' : 'none'} />
                            <span>Home</span>
                          </button>
                          <button
                            onClick={() => setCurrentAppTab('HOME')}
                            className="flex flex-col items-center gap-0.5 text-[10px] font-semibold text-[#9aa3bb]"
                          >
                            <Mic className="w-[18px] h-[18px]" strokeWidth={2.2} />
                            <span>Practice</span>
                          </button>
                          <button
                            onClick={() => setCurrentAppTab('FRIENDS')}
                            className={`flex flex-col items-center gap-0.5 text-[10px] ${currentAppTab === 'FRIENDS' ? 'font-bold text-[#3d6ef5]' : 'font-semibold text-[#9aa3bb]'}`}
                          >
                            <Users className="w-[18px] h-[18px]" strokeWidth={2.4} fill={currentAppTab === 'FRIENDS' ? 'currentColor' : 'none'} />
                            <span>Friends</span>
                          </button>
                          <button
                            onClick={() => setCurrentAppTab('SUBSCRIPTION')}
                            className="flex flex-col items-center gap-0.5 text-[10px] font-semibold text-[#9aa3bb]"
                          >
                            <BarChart3 className="w-[18px] h-[18px]" strokeWidth={2.2} />
                            <span>Progress</span>
                          </button>
                          <button
                            onClick={() => setCurrentAppTab('PROFILE')}
                            className={`flex flex-col items-center gap-0.5 text-[10px] ${currentAppTab === 'PROFILE' ? 'font-bold text-[#3d6ef5]' : 'font-semibold text-[#9aa3bb]'}`}
                          >
                            <User className="w-[18px] h-[18px]" strokeWidth={2.4} fill={currentAppTab === 'PROFILE' ? 'currentColor' : 'none'} />
                            <span>Profile</span>
                          </button>
                        </div>
                      ) : (
                      <div className="h-14 bg-[#0d1017] border-t border-slate-800/90 px-2 flex items-center justify-around shrink-0 z-10">
                        <button
                          onClick={() => setCurrentAppTab('HOME')}
                          className={`flex flex-col items-center gap-0.5 text-[10px] font-semibold transition-all ${
                            currentAppTab === 'HOME' ? 'text-emerald-400' : 'text-slate-400 hover:text-slate-200'
                          }`}
                        >
                          <Home className="w-4 h-4" />
                          <span>Home</span>
                        </button>

                        <button
                          onClick={() => setCurrentAppTab('FRIENDS')}
                          className={`flex flex-col items-center gap-0.5 text-[10px] font-semibold transition-all ${
                            currentAppTab === 'FRIENDS' ? 'text-emerald-400' : 'text-slate-400 hover:text-slate-200'
                          }`}
                        >
                          <Users className="w-4 h-4" />
                          <span>Friends</span>
                        </button>

                        <button
                          onClick={() => setCurrentAppTab('SUBSCRIPTION')}
                          className={`flex flex-col items-center gap-0.5 text-[10px] font-semibold transition-all ${
                            currentAppTab === 'SUBSCRIPTION' ? 'text-amber-400' : 'text-slate-400 hover:text-slate-200'
                          }`}
                        >
                          <Crown className="w-4 h-4" />
                          <span>5-Mo PRO</span>
                        </button>

                        <button
                          onClick={() => setCurrentAppTab('PROFILE')}
                          className={`flex flex-col items-center gap-0.5 text-[10px] font-semibold transition-all ${
                            currentAppTab === 'PROFILE' ? 'text-emerald-400' : 'text-slate-400 hover:text-slate-200'
                          }`}
                        >
                          <User className="w-4 h-4" />
                          <span>Profile</span>
                        </button>
                      </div>
                      )}
                    </div>
                  )}

                  {/* Direct Chat Modal Bottom Sheet */}
                  {activeChatFriend && (
                    <div className="absolute inset-0 bg-[#0a0d13] z-40 flex flex-col">
                      {/* Chat Header */}
                      <div className="px-4 py-3 bg-[#0e1219] border-b border-slate-800 flex items-center justify-between">
                        <div className="flex items-center gap-2.5">
                          <div className={`w-8 h-8 rounded-full ${activeChatFriend.avatarColor} flex items-center justify-center text-xs font-bold text-white`}>
                            {activeChatFriend.name.charAt(0)}
                          </div>
                          <div>
                            <h4 className="text-xs font-bold text-white">{activeChatFriend.name}</h4>
                            <span className="text-[10px] text-emerald-400">Online • English Buddy</span>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => {
                              const friend = activeChatFriend;
                              setActiveChatFriend(null);
                              handleStartDirectCall(friend);
                            }}
                            className="p-1.5 rounded-lg bg-emerald-500 text-slate-950 font-bold"
                            title="Start Voice Call"
                          >
                            <Phone className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setActiveChatFriend(null)}
                            className="p-1.5 rounded-lg bg-slate-800 text-slate-400 hover:text-white"
                          >
                            <X className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      {/* Messages Stream */}
                      <div className="flex-1 overflow-y-auto p-3 space-y-2">
                        {chatMessages.map(msg => (
                          <div
                            key={msg.id}
                            className={`flex ${msg.sender === 'me' ? 'justify-end' : 'justify-start'}`}
                          >
                            <div
                              className={`max-w-[80%] rounded-2xl px-3 py-2 text-xs ${
                                msg.sender === 'me'
                                  ? 'bg-emerald-600 text-white rounded-br-none'
                                  : 'bg-slate-800 text-slate-200 rounded-bl-none'
                              }`}
                            >
                              <p>{msg.text}</p>
                              <span className="text-[9px] opacity-70 block text-right mt-0.5">{msg.time}</span>
                            </div>
                          </div>
                        ))}
                      </div>

                      {/* Message Input Bar */}
                      <form onSubmit={handleSendMessage} className="p-2.5 bg-[#0e1219] border-t border-slate-800 flex gap-2">
                        <input
                          type="text"
                          placeholder="Type English message..."
                          value={newMsgText}
                          onChange={e => setNewMsgText(e.target.value)}
                          className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
                        />
                        <button
                          type="submit"
                          className="w-8 h-8 rounded-xl bg-emerald-500 text-slate-950 flex items-center justify-center font-bold"
                        >
                          <Send className="w-3.5 h-3.5" />
                        </button>
                      </form>
                    </div>
                  )}

                  {showPrivacyScreen && (
                    <div className="absolute inset-0 bg-[#0a0d13] z-40 flex flex-col">
                      <div className="px-4 py-3 bg-[#0e1219] border-b border-slate-800 flex items-center justify-between">
                        <h4 className="text-xs font-bold text-white">Privacy Policy</h4>
                        <button
                          onClick={() => setShowPrivacyScreen(false)}
                          className="p-1.5 rounded-lg bg-slate-800 text-slate-400 hover:text-white"
                        >
                          <X className="w-3.5 h-3.5" />
                        </button>
                      </div>
                      <div className="flex-1 overflow-y-auto p-4 space-y-3 text-[11px] text-slate-300 leading-relaxed">
                        <p className="text-slate-500">Last updated: September 5, 2026</p>
                        <p>SpeakFree is an English speaking practice app that lets users make anonymous live voice calls with other learners.</p>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">1. Age requirement</h5>
                          <p>SpeakFree is for users 18 years of age or older. Random voice chat with strangers is not intended for children. By using the app you confirm that you are at least 18.</p>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">2. Information we collect</h5>
                          <ul className="list-disc pl-4 space-y-1">
                            <li>Account details you provide (name, email) if you sign up</li>
                            <li>Microphone audio during a live call (streamed peer-to-peer, not stored on our servers)</li>
                            <li>Temporary matchmaking data in Firebase Firestore while connecting a call</li>
                            <li>Google Play purchase receipts if you buy the VIP pass</li>
                            <li>Device identifiers needed for Firebase and Google Play Billing</li>
                          </ul>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">3. Microphone and voice calls</h5>
                          <p>Microphone permission is required for live voice practice. Audio is sent directly between devices using WebRTC. We do not record, store, or sell call audio.</p>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">4. Random stranger voice chat</h5>
                          <p>Find Partner matches you with another learner at random. Do not share personal information. You can Report or Block a partner during a call.</p>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">5. User-generated content (UGC) safety</h5>
                          <p>Voice and in-app chat are user-generated. Users can report or block a partner. We review reports and may restrict accounts that violate these rules.</p>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">6. How data is used</h5>
                          <p>We use data only to run matchmaking, authentication, purchases, and safety features. Temporary signaling documents are deleted after a call connects.</p>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">7. Sharing</h5>
                          <p>We share data with Google Firebase (auth, Firestore) and Google Play Billing as needed to operate the app. We do not sell personal data.</p>
                        </div>
                        <div>
                          <h5 className="text-xs font-bold text-white mb-1">8. Contact</h5>
                          <p>For privacy or safety requests, contact the developer through the SpeakFree Google Play listing.</p>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Phone OTP Login Modal */}
                  {showAuthModal && (
                    <div className="absolute inset-0 bg-black/80 backdrop-blur-xs z-50 flex items-center justify-center p-4">
                      <div className="w-full max-w-[300px] bg-[#10141d] border border-slate-700 rounded-2xl p-5 shadow-2xl relative">
                        <button
                          onClick={() => {
                            setShowAuthModal(false);
                            setAuthStep('login');
                          }}
                          className="absolute top-3.5 right-3.5 text-slate-400 hover:text-white"
                        >
                          <X className="w-4 h-4" />
                        </button>

                        <div className="text-center mb-4">
                          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto mb-2 border border-emerald-500/30">
                            <Mail className="w-5 h-5" />
                          </div>
                          <h3 className="text-sm font-bold text-white">
                            {authStep === 'login' ? 'Login with Email' : 'Create Account'}
                          </h3>
                          <p className="text-[11px] text-slate-400 mt-0.5">
                            Secure authentication to save progress
                          </p>
                        </div>

                        {authError && (
                          <div className="mb-3 p-2 bg-red-500/10 border border-red-500/20 rounded-lg text-[10px] text-red-400">
                            {authError}
                          </div>
                        )}

                        {authSuccess && (
                          <div className="mb-3 p-2 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-[10px] text-emerald-400">
                            {authSuccess}
                          </div>
                        )}

                        {authStep === 'login' && (
                          <form onSubmit={handleEmailLogin} className="space-y-3">
                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1">Email Address</label>
                              <input
                                type="email"
                                placeholder="yourname@example.com"
                                value={inputEmail}
                                onChange={e => setInputEmail(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-emerald-500"
                                required
                              />
                            </div>

                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1">Password</label>
                              <input
                                type="password"
                                placeholder="••••••••"
                                value={inputPassword}
                                onChange={e => setInputPassword(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-emerald-500"
                                required
                              />
                            </div>

                            <button
                              type="submit"
                              className="w-full py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold transition-all shadow-md mt-2"
                            >
                              Log In
                            </button>

                            <p className="text-[10px] text-center text-slate-400 mt-2">
                              New to SpeakFree?{' '}
                              <span
                                className="text-emerald-400 cursor-pointer hover:underline font-semibold"
                                onClick={() => {
                                  setAuthStep('signup');
                                  setAuthError('');
                                  setAuthSuccess('');
                                }}
                              >
                                Sign Up
                              </span>
                            </p>
                          </form>
                        )}

                        {authStep === 'signup' && (
                          <form onSubmit={handleEmailSignUp} className="space-y-3">
                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1">Display Name</label>
                              <input
                                type="text"
                                placeholder="e.g. Harish Singh"
                                value={inputName}
                                onChange={e => setInputName(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-emerald-500"
                                required
                              />
                            </div>

                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1">Email Address</label>
                              <input
                                type="email"
                                placeholder="yourname@example.com"
                                value={inputEmail}
                                onChange={e => setInputEmail(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-emerald-500"
                                required
                              />
                            </div>

                            <div>
                              <label className="text-[10px] text-slate-400 block mb-1">Password</label>
                              <input
                                type="password"
                                placeholder="Min. 6 characters"
                                value={inputPassword}
                                onChange={e => setInputPassword(e.target.value)}
                                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-600 focus:outline-none focus:border-emerald-500"
                                required
                              />
                            </div>

                            <button
                              type="submit"
                              className="w-full py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold transition-all shadow-md mt-2"
                            >
                              Sign Up
                            </button>

                            <p className="text-[10px] text-center text-slate-400 mt-2">
                              Already have an account?{' '}
                              <span
                                className="text-emerald-400 cursor-pointer hover:underline font-semibold"
                                onClick={() => {
                                  setAuthStep('login');
                                  setAuthError('');
                                  setAuthSuccess('');
                                }}
                              >
                                Log In
                              </span>
                            </p>
                          </form>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Google Play Billing v7 Purchase Sheet (Official Simulated Sheet) */}
                  {showPlayBillingSheet && (
                    <div className="absolute inset-0 bg-black/80 backdrop-blur-xs z-50 flex flex-col justify-end">
                      <div className="w-full bg-[#181d28] border-t border-slate-700 rounded-t-3xl p-5 shadow-2xl relative animate-in slide-in-from-bottom duration-200">
                        <div className="w-10 h-1 bg-slate-700 rounded-full mx-auto mb-3" />

                        {/* Google Play Header */}
                        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                          <div className="flex items-center gap-2">
                            <div className="w-6 h-6 rounded bg-emerald-500/20 flex items-center justify-center text-emerald-400 font-bold text-xs">
                              ▶
                            </div>
                            <span className="text-xs font-bold text-white tracking-wide">Google Play</span>
                          </div>
                          <button
                            onClick={() => !isVerifyingReceipt && setShowPlayBillingSheet(false)}
                            className="text-slate-400 hover:text-white"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </div>

                        {/* Product Info */}
                        <div className="py-3">
                          <div className="flex items-start justify-between">
                            <div>
                              <h4 className="text-sm font-bold text-white">SpeakFree VIP Pass (5 Months)</h4>
                              <p className="text-[11px] text-slate-400 mt-0.5">speakfree_vip_5months</p>
                            </div>
                            <div className="text-right">
                              <span className="text-sm font-extrabold text-amber-400">₹100.00</span>
                              <p className="text-[9px] text-slate-400">Tax included</p>
                            </div>
                          </div>

                          <div className="mt-3 p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-[10px] text-slate-300 space-y-1">
                            <div className="flex items-center justify-between text-slate-400">
                              <span>Payment Account:</span>
                              <span className="text-slate-200 font-mono">{userEmail || 'learner@play.google.com'}</span>
                            </div>
                            <div className="flex items-center justify-between text-slate-400">
                              <span>Security Verification:</span>
                              <span className="text-emerald-400 font-medium">Google Play Token Verification</span>
                            </div>
                          </div>
                        </div>

                        {/* CTA Buttons */}
                        <div className="space-y-2 pt-2">
                          <button
                            onClick={handleConfirmPlayPurchase}
                            disabled={isVerifyingReceipt}
                            className="w-full py-3 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-400 hover:brightness-110 text-slate-950 font-bold text-xs flex items-center justify-center gap-2 transition-all shadow-lg cursor-pointer disabled:opacity-60"
                          >
                            {isVerifyingReceipt ? (
                              <>
                                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                                <span>Verifying Receipt Token...</span>
                              </>
                            ) : (
                              <>
                                <ShieldCheck className="w-4 h-4" />
                                <span>1-Tap Subscribe (Google Play)</span>
                              </>
                            )}
                          </button>

                          <button
                            onClick={() => !isVerifyingReceipt && setShowPlayBillingSheet(false)}
                            disabled={isVerifyingReceipt}
                            className="w-full py-2 text-[11px] text-slate-400 hover:text-slate-200 font-medium"
                          >
                            Cancel
                          </button>
                        </div>

                        <p className="text-[9px] text-slate-500 text-center mt-1 leading-tight">
                          Billing library v7 compliant. Manage or cancel anytime in Google Play &gt; Payments &amp; subscriptions.
                        </p>
                      </div>
                    </div>
                  )}

                  {/* Phone Bottom Pill Indicator */}
                  <div className="w-24 h-1 bg-slate-800 rounded-full mx-auto my-1.5 shrink-0 z-30" />
                </div>
              </div>
            </div>

            {/* Right Column: Live Telemetry, Firestore Stats & APK Build */}
            <div className="lg:col-span-7 space-y-6">
              
              {/* Architecture Highlights Card */}
              <div className="p-5 rounded-2xl bg-[#0e1219] border border-slate-800/80 shadow-lg">
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <Database className="w-5 h-5 text-emerald-400" />
                    <h3 className="font-bold text-sm text-white">Live Zero-Cost Architecture Status</h3>
                  </div>
                  <span className="text-xs px-2.5 py-1 rounded-md bg-emerald-500/10 text-emerald-400 font-mono border border-emerald-500/20">
                    Firebase Spark Free Tier
                  </span>
                </div>

                <p className="text-xs text-slate-300 leading-relaxed">
                  Full English Learning Android application built with <strong>Jetpack Compose</strong> and <strong>WebRTC P2P Direct Audio</strong>. Matches random learners worldwide with ephemeral Firestore signaling that auto-purges instantly on connection for $0.00/month operation.
                </p>

                {/* 3 Metric Cards */}
                <div className="grid grid-cols-3 gap-3 mt-4">
                  <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 text-center">
                    <span className="text-[10px] text-slate-400 block font-medium">Server Cost</span>
                    <span className="text-sm font-bold text-emerald-400 font-mono">₹0 / month</span>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 text-center">
                    <span className="text-[10px] text-slate-400 block font-medium">Daily Free Calls</span>
                    <span className="text-sm font-bold text-emerald-400 font-mono">12,500+</span>
                  </div>
                  <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 text-center">
                    <span className="text-[10px] text-slate-400 block font-medium">Target APK Size</span>
                    <span className="text-sm font-bold text-emerald-400 font-mono">&lt; 8 MB</span>
                  </div>
                </div>
              </div>

              {/* Real-time Signaling & Purge Log Stream */}
              <div className="p-4 rounded-2xl bg-[#090c10] border border-slate-800 shadow-inner">
                <div className="flex items-center justify-between pb-3 mb-2 border-b border-slate-800/80">
                  <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
                    <Activity className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Live Signaling & Purge Stream</span>
                  </div>
                  <button
                    onClick={() => setDbLogs([])}
                    className="text-[11px] text-slate-500 hover:text-slate-300 transition-colors"
                  >
                    Clear Log
                  </button>
                </div>

                <div className="space-y-1.5 font-mono text-xs max-h-52 overflow-y-auto pr-1">
                  {dbLogs.map((log, index) => (
                    <div key={index} className="flex items-start gap-2 py-0.5">
                      <span className="text-slate-600 text-[10px] shrink-0">{log.time}</span>
                      <span
                        className={`px-1.5 py-0.2 rounded text-[10px] uppercase font-bold shrink-0 ${
                          log.type === 'delete'
                            ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                            : log.type === 'write'
                            ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                            : log.type === 'read'
                            ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                            : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        }`}
                      >
                        {log.type}
                      </span>
                      <span className="text-slate-300 text-xs">{log.action}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Android APK GitHub Actions Ready Banner */}
              <div className="p-4 rounded-2xl bg-gradient-to-r from-slate-900 to-[#0e131d] border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                    <h4 className="text-xs font-bold text-white">GitHub Actions APK Workflow Created</h4>
                  </div>
                  <p className="text-[11px] text-slate-400 mt-0.5">
                    <code>.github/workflows/main.yml</code> automatically builds <code>app-debug.apk</code> on GitHub!
                  </p>
                </div>
                <button
                  onClick={() => { setActiveTab('code'); setSelectedFileKey('MainActivity.kt'); }}
                  className="px-3 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-xs flex items-center gap-1 shrink-0"
                >
                  <FileCode className="w-3.5 h-3.5" />
                  View Kotlin Files
                </button>
              </div>
            </div>
          </div>
        )}

        {/* CODE TAB */}
        {activeTab === 'code' && (
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            <div className="lg:col-span-4 space-y-2">
              <div className="p-3 rounded-xl bg-slate-900/90 border border-slate-800 mb-3">
                <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-1">Android Project Structure</h3>
                <p className="text-[11px] text-slate-400">Jetpack Compose • Kotlin • WebRTC</p>
              </div>

              {[
                { name: 'MainActivity.kt', path: 'app/src/main/java/com/speakfreeenglish/app/MainActivity.kt', desc: 'Main scaffold with bottom navigation, auth dialog, and call state transitions.' },
                { name: 'HomeScreen.kt', path: 'app/src/main/java/com/speakfreeenglish/app/ui/screens/HomeScreen.kt', desc: 'Pulsing find partner action, topic cards, and learner status.' },
                { name: 'FriendsScreen.kt', path: 'app/src/main/java/com/speakfreeenglish/app/ui/screens/FriendsScreen.kt', desc: 'Online English speaking partners, direct calling, and instant chat.' },
                { name: 'AuthDialog.kt', path: 'app/src/main/java/com/speakfreeenglish/app/ui/screens/AuthDialog.kt', desc: 'Email & Password authentication and verification dialog.' },
                { name: 'ProfileScreen.kt', path: 'app/src/main/java/com/speakfreeenglish/app/ui/screens/ProfileScreen.kt', desc: 'Learner profile with Gallery photo picker, email sign up/login, and settings.' },
                { name: 'SubscriptionScreen.kt', path: 'app/src/main/java/com/speakfreeenglish/app/ui/screens/SubscriptionScreen.kt', desc: '5-Month English Booster Pack with Google Play Billing v7 purchase flow.' },
                { name: 'PlayBillingManager.kt', path: 'app/src/main/java/com/speakfreeenglish/app/billing/PlayBillingManager.kt', desc: 'Official Google Play Billing Library v7 manager with receipt verification.' },
                { name: 'WebRtcAudioClient.kt', path: 'app/src/main/java/com/speakfreeenglish/app/webrtc/WebRtcAudioClient.kt', desc: 'Zero-cost WebRTC P2P audio client with Google STUN servers.' }
              ].map(file => (
                <button
                  key={file.name}
                  onClick={() => setSelectedFileKey(file.name)}
                  className={`w-full text-left p-3 rounded-xl border transition-all flex flex-col gap-1 cursor-pointer ${
                    selectedFileKey === file.name
                      ? 'bg-emerald-500/10 border-emerald-500/40 text-emerald-300 shadow-md'
                      : 'bg-[#0d1017] border-slate-800/80 text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-semibold text-white">{file.name}</span>
                    <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-400 uppercase font-mono">
                      Kotlin
                    </span>
                  </div>
                  <span className="text-[11px] text-slate-500 font-mono truncate">{file.path}</span>
                </button>
              ))}
            </div>

            <div className="lg:col-span-8 rounded-2xl bg-[#0a0d13] border border-slate-800 overflow-hidden shadow-2xl flex flex-col">
              <div className="px-5 py-3.5 bg-[#0e1219] border-b border-slate-800 flex items-center justify-between">
                <div>
                  <h3 className="font-mono text-sm font-bold text-white flex items-center gap-2">
                    <FileCode className="w-4 h-4 text-emerald-400" />
                    {selectedFileKey}
                  </h3>
                  <p className="text-xs text-slate-400 mt-0.5">Android Native Jetpack Compose Source</p>
                </div>
              </div>

              <div className="p-4 overflow-x-auto max-h-[600px] overflow-y-auto bg-[#07090e]">
                <pre className="font-mono text-xs text-slate-300 leading-relaxed">
                  <code>{`// File: ${selectedFileKey}
// Complete Kotlin and Jetpack Compose source is available in /app/src/main/java/com/speakfreeenglish/app/

package com.speakfreeenglish.app

// Ready for Android Studio & GitHub Actions compilation`}</code>
                </pre>
              </div>
            </div>
          </div>
        )}

        {/* ARCHITECTURE TAB */}
        {activeTab === 'architecture' && (
          <div className="max-w-4xl mx-auto space-y-6">
            <div className="p-6 rounded-2xl bg-[#0e1219] border border-slate-800">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
                  <Flame className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-white">How Zero Monthly Cost Is Guaranteed</h3>
                  <p className="text-xs text-slate-400">Firebase Spark Free Tier + Pure P2P Audio</p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6">
                <div className="p-4 rounded-xl bg-slate-900/80 border border-slate-800">
                  <h4 className="text-sm font-bold text-emerald-400 flex items-center gap-2 mb-2">
                    <Database className="w-4 h-4" /> 1. Instant Firestore Cleanup
                  </h4>
                  <p className="text-xs text-slate-300 leading-relaxed">
                    Signaling documents exist only for 2 seconds during handshake. The millisecond WebRTC connects, documents are deleted. Zero long-term database storage.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-slate-900/80 border border-slate-800">
                  <h4 className="text-sm font-bold text-emerald-400 flex items-center gap-2 mb-2">
                    <ServerOff className="w-4 h-4" /> 2. Google Public STUN
                  </h4>
                  <p className="text-xs text-slate-300 leading-relaxed">
                    Audio is encrypted and streamed device-to-device via Google's free STUN server (<code className="text-emerald-300">stun.l.google.com:19302</code>).
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
