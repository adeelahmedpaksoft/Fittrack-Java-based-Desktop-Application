====================================================
  FitTrack Pro 
  Fitness, Habit & Activity Tracking System
====================================================

HOW TO RUN IN ECLIPSE
----------------------
1. File > Import > General > Existing Projects into Workspace
2. Browse to this folder → Finish
3. Right-click src/fittrack/Main.java → Run As → Java Application

OR run the JAR directly:
  java -jar FitTrackPro.jar

====================================================
LOGIN CREDENTIALS
====================================================
  Role      | Username | Password  | Goal
  ----------|----------|-----------|------------------
  Admin     | admin    | admin123  | General Fitness
  Athlete   | alex     | fit123    | Endurance
  Athlete   | sam      | gym456    | Muscle Gain
  Athlete   | morgan   | yoga789   | Weight Loss

Admin: full access including Settings panel and delete
Athlete: log workouts, manage habits, view progress

====================================================
FEATURES
====================================================
  ⚡ Animated dark login with quick-login buttons
  🏠 Dashboard: stat cards, weekly calorie bar chart,
     workout type donut chart, recent activity feed
  🏋 Workout Log: full table, 8 filter/sort strategies,
     log/edit/delete sessions with undo/redo
  ✅ Habit Tracker: 14-day heatmap cards, streak counter,
     one-click toggle, quick add new habits
  📈 Progress: goal rings, 30-day activity heatmap,
     personal bests, workout type breakdown chart
  ⚙  Settings: profile, fitness goals config (Singleton),
     admin user panel
  💾 File persistence: workouts.dat + habits.dat (CSV)
  📋 Activity log: fittrack.log

====================================================
DESIGN PATTERNS
====================================================
  Singleton → AppConfig: single shared config, persists
              goals & paths to fittrack_config.properties
  Observer  → FitnessObserver: Dashboard, WorkoutPanel,
              HabitPanel, ProgressPanel, ActivityLogger
              all auto-refresh on every data change
  Strategy  → 8 WorkoutStrategy implementations:
              All, This Week, This Month, Cardio, Strength,
              High Intensity, Most Calories, Longest First
  Command   → LogWorkout, RemoveWorkout, AddHabit,
              ToggleHabit — all with undo/redo via
              CommandManager stack

====================================================
PROJECT STRUCTURE (24 classes, 9 packages)
====================================================
  fittrack/
  ├── Main.java
  ├── auth/       AuthService (SHA-256 login)
  ├── model/      User, WorkoutSession, Habit,
  │               FitnessRepository
  ├── observer/   FitnessObserver, ActivityLogger
  ├── singleton/  AppConfig
  ├── strategy/   WorkoutStrategy + 8 strategies
  ├── command/    Command + 4 commands + CommandManager
  ├── persistence/FitnessPersistenceService (CSV)
  └── ui/
      ├── components/ Theme, UI helpers, ScrollBar
      ├── login/      LoginPanel
      ├── dashboard/  DashboardPanel, Charts
      ├── workout/    WorkoutPanel, WorkoutFormDialog
      ├── habit/      HabitPanel
      ├── progress/   ProgressPanel + 2 chart classes
      ├── settings/   SettingsPanel
      ├── SidebarPanel
      └── MainFrame
====================================================
