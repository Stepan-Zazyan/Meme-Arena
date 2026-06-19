import 'package:flutter/foundation.dart';

class ApiConfig {
  ApiConfig({String? baseUrl}) : baseUrl = _resolve(baseUrl);
  final String baseUrl;

  static String _resolve(String? explicit) {
    const configured = String.fromEnvironment('API_BASE_URL');
    final value = (explicit ?? (configured.isEmpty ? 'http://10.0.2.2:8080' : configured)).replaceFirst(RegExp(r'/+$'), '');
    if (kReleaseMode) {
      if (!value.startsWith('https://')) {
        throw StateError('Release API_BASE_URL must use HTTPS');
      }
      if (value.contains('localhost') || value.contains('10.0.2.2')) {
        throw StateError('Release API_BASE_URL must not point to local development hosts');
      }
    }
    return value;
  }
}
