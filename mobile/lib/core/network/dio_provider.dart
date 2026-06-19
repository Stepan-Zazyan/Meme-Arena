import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import 'session_store.dart';

final apiConfigProvider = Provider((_) => ApiConfig());
final sessionStoreProvider = Provider((_) => SessionStore(const FlutterSecureStorage()));
final authExpiredProvider = StateProvider<int>((_) => 0);
final dioProvider = Provider((ref) {
  final dio = Dio(BaseOptions(baseUrl: ref.watch(apiConfigProvider).baseUrl, connectTimeout: const Duration(seconds: 10), receiveTimeout: const Duration(seconds: 20), sendTimeout: const Duration(seconds: 20)));
  final store = ref.watch(sessionStoreProvider);
  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) async {
      options.headers['X-Request-Id'] = 'ma-${DateTime.now().microsecondsSinceEpoch}';
      final isGuest = options.path == '/api/v1/users/guest';
      if (!isGuest) { final token = await store.accessToken(); if (token != null && token.isNotEmpty) options.headers['Authorization'] = 'Bearer $token'; }
      if (kDebugMode) debugPrint('${options.method} ${options.uri} requestId=${options.headers['X-Request-Id']}');
      handler.next(options);
    },
    onResponse: (response, handler) { if (kDebugMode) debugPrint('${response.statusCode} ${response.requestOptions.uri} requestId=${response.headers.value('X-Request-Id')}'); handler.next(response); },
    onError: (error, handler) async { if (error.response?.statusCode == 401) { await store.clearUserSession(); ref.read(authExpiredProvider.notifier).state++; } handler.next(error); },
  ));
  return dio;
});
