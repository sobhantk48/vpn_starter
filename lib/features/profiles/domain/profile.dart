import 'package:equatable/equatable.dart';

class VpnProfile extends Equatable {
  final String id;
  final String name;
  final String type;
  final String rawConfig;
  final bool isDefault;

  const VpnProfile({
    required this.id,
    required this.name,
    required this.type,
    required this.rawConfig,
    this.isDefault = false,
  });

  @override
  List<Object?> get props => [id, name, type, rawConfig, isDefault];
}
