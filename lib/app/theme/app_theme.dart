import 'package:flutter/material.dart';

class AppTheme {
  static const Color _seedColor = Colors.teal;

  static ThemeData get light => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: _seedColor),
        scaffoldBackgroundColor: const Color(0xFFF6F7F8),
      );

  static ThemeData get dark => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: _seedColor,
          brightness: Brightness.dark,
        ),
      );
}
