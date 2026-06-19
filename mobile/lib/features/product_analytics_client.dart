import 'package:dio/dio.dart';

class ProductAnalyticsClient {
  ProductAnalyticsClient(this._dio);
  final Dio _dio;
  static const _allowed = {
    'APP_OPENED','ONBOARDING_COMPLETED','BATTLE_OPENED','VOTE_COMPLETED','TOP_OPENED','MEME_UPLOAD_COMPLETED','PROFILE_OPENED','SCOUT_PREDICTIONS_OPENED','TOURNAMENT_OPENED','TOURNAMENT_VOTE_COMPLETED'
  };
  void track(String eventType, {String appVersion = '0.1.0', String platform = 'ANDROID'}) {
    if (!_allowed.contains(eventType)) return;
    Future<void>(() async {
      try {
        await _dio.post('/api/v1/product-events', data: {
          'eventType': eventType,
          'occurredAt': DateTime.now().toUtc().toIso8601String(),
          'appVersion': appVersion,
          'platform': platform,
        });
      } catch (_) {
        // Best-effort first-party analytics must never block or break UI flows.
      }
    });
  }
}
