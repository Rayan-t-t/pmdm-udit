import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';

import '../data/contenidos.dart';
import '../models/contenido.dart';

// Reproductor de vídeo integrado en UDITflix.
// La URL de demostración se definió en data/contenidos.dart.
class ReproductorScreen extends StatefulWidget {
  const ReproductorScreen({super.key, required this.contenido});

  final Contenido contenido;

  @override
  State<ReproductorScreen> createState() => _ReproductorScreenState();
}

class _ReproductorScreenState extends State<ReproductorScreen> {
  VideoPlayerController? _controller;
  String? _error;

  @override
  void initState() {
    super.initState();
    _initialize();
  }

  Future<void> _initialize() async {
    _controller?.dispose();
    _controller = null;
    final url = widget.contenido.videoUrl ?? videoDemo;
    final controller = VideoPlayerController.networkUrl(Uri.parse(url));

    try {
      await controller.initialize();
      if (!mounted) {
        await controller.dispose();
        return;
      }
      setState(() {
        _controller = controller;
        _error = null;
      });
      await controller.play();
    } catch (_) {
      await controller.dispose();
      if (mounted) {
        setState(() {
          _error = 'No se pudo cargar el vídeo. Comprueba tu conexión.';
        });
      }
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  void _seek(VideoPlayerController controller, int seconds) {
    final duration = controller.value.duration;
    final target = controller.value.position + Duration(seconds: seconds);
    if (target < Duration.zero) {
      controller.seekTo(Duration.zero);
    } else if (target > duration) {
      controller.seekTo(duration);
    } else {
      controller.seekTo(target);
    }
  }

  String _format(Duration duration) {
    final minutes = duration.inMinutes.remainder(60).toString().padLeft(2, '0');
    final seconds = duration.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  void _openFullscreen(VideoPlayerController controller) {
    Navigator.of(context).push(
      MaterialPageRoute<bool>(
        fullscreenDialog: true,
        builder: (_) => VideoFullscreenScreen(controller: controller),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final controller = _controller;

    return Scaffold(
      appBar: AppBar(title: Text(widget.contenido.titulo)),
      body: SafeArea(
        child: controller == null
            ? _buildLoadingOrError()
            : _buildPlayer(controller),
      ),
    );
  }

  Widget _buildLoadingOrError() {
    final error = _error;
    if (error == null) {
      return const Center(child: CircularProgressIndicator());
    }

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.error_outline,
              size: 56,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 16),
            Text(error, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: _initialize,
              icon: const Icon(Icons.refresh),
              label: const Text('Reintentar'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPlayer(VideoPlayerController controller) {
    return Column(
      children: [
        Expanded(
          child: Container(
            color: Colors.black,
            alignment: Alignment.center,
            child: AspectRatio(
              aspectRatio: controller.value.aspectRatio == 0
                  ? 16 / 9
                  : controller.value.aspectRatio,
              child: VideoPlayer(controller),
            ),
          ),
        ),
        ValueListenableBuilder<VideoPlayerValue>(
          valueListenable: controller,
          builder: (context, value, _) {
            final maxMillis = value.duration.inMilliseconds > 0
                ? value.duration.inMilliseconds.toDouble()
                : 1.0;
            final currentMillis = value.position.inMilliseconds < 0
                ? 0.0
                : value.position.inMilliseconds.toDouble();

            return Container(
              color: const Color(0xFFF0F3F5),
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Slider(
                    value: currentMillis.clamp(0.0, maxMillis).toDouble(),
                    max: maxMillis,
                    activeColor: Theme.of(context).colorScheme.primary,
                    onChanged: value.duration.inMilliseconds > 0
                        ? (millis) => controller.seekTo(
                              Duration(milliseconds: millis.round()),
                            )
                        : null,
                  ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        _format(value.position),
                        style: const TextStyle(color: Color(0xFF202A33)),
                      ),
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          IconButton(
                            tooltip: 'Retroceder 10 s',
                            icon: const Icon(Icons.replay_10),
                            onPressed: () => _seek(controller, -10),
                          ),
                          IconButton(
                            tooltip: value.isPlaying ? 'Pausar' : 'Reproducir',
                            iconSize: 36,
                            icon: Icon(
                              value.isPlaying
                                  ? Icons.pause_circle_filled
                                  : Icons.play_circle_filled,
                            ),
                            onPressed: () => value.isPlaying
                                ? controller.pause()
                                : controller.play(),
                          ),
                          IconButton(
                            tooltip: 'Avanzar 10 s',
                            icon: const Icon(Icons.forward_10),
                            onPressed: () => _seek(controller, 10),
                          ),
                          IconButton(
                            tooltip: 'Pantalla completa',
                            icon: const Icon(Icons.fullscreen),
                            onPressed: () => _openFullscreen(controller),
                          ),
                        ],
                      ),
                      Text(
                        _format(value.duration),
                        style: const TextStyle(color: Color(0xFF202A33)),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        ),
      ],
    );
  }
}

// Vista a pantalla completa: reutiliza el mismo controlador para no
// reiniciar el vídeo. No lo libera, porque sigue vivo en la pantalla anterior.
class VideoFullscreenScreen extends StatefulWidget {
  const VideoFullscreenScreen({super.key, required this.controller});

  final VideoPlayerController controller;

  @override
  State<VideoFullscreenScreen> createState() => _VideoFullscreenScreenState();
}

class _VideoFullscreenScreenState extends State<VideoFullscreenScreen> {
  @override
  void initState() {
    super.initState();
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
  }

  @override
  void dispose() {
    SystemChrome.setPreferredOrientations(DeviceOrientation.values);
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = widget.controller.value.size;

    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          Positioned.fill(
            child: FittedBox(
              fit: BoxFit.contain,
              child: SizedBox(
                width: size.width == 0 ? 16 : size.width,
                height: size.height == 0 ? 9 : size.height,
                child: VideoPlayer(widget.controller),
              ),
            ),
          ),
          Positioned(
            top: 16,
            right: 16,
            child: IconButton(
              tooltip: 'Salir de pantalla completa',
              icon: const Icon(Icons.close, color: Colors.white, size: 32),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ),
          Positioned(
            bottom: 24,
            left: 24,
            child: ValueListenableBuilder<VideoPlayerValue>(
              valueListenable: widget.controller,
              builder: (context, value, _) => IconButton(
                tooltip: value.isPlaying ? 'Pausar' : 'Reproducir',
                iconSize: 40,
                color: Colors.white,
                icon: Icon(
                  value.isPlaying ? Icons.pause_circle_filled : Icons.play_circle_filled,
                ),
                onPressed: () => value.isPlaying
                    ? widget.controller.pause()
                    : widget.controller.play(),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
