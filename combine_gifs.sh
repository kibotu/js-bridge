#!/usr/bin/env bash
set -euo pipefail

if ! command -v ffmpeg &>/dev/null; then
  echo "Error: ffmpeg is not installed." >&2
  exit 1
fi

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <left.gif> <right.gif>" >&2
  exit 1
fi

LEFT="$1"
RIGHT="$2"
GAP=32
MAX_H=420
OUT="combined.gif"

LEFT_DUR=$(ffprobe -v quiet -show_entries format=duration -of csv=p=0 "$LEFT")
RIGHT_DUR=$(ffprobe -v quiet -show_entries format=duration -of csv=p=0 "$RIGHT")

LEFT_FRAMES=$(ffprobe -v quiet -show_entries stream=nb_frames -of csv=p=0 "$LEFT")
RIGHT_FRAMES=$(ffprobe -v quiet -show_entries stream=nb_frames -of csv=p=0 "$RIGHT")

LOOPS_FOR_RIGHT=$(python3 -c "import math; print(math.ceil($LEFT_DUR / $RIGHT_DUR))")
LOOPS_FOR_LEFT=$(python3 -c "import math; print(math.ceil($RIGHT_DUR / $LEFT_DUR))")
MAX_FRAMES=$(python3 -c "print(max($LEFT_FRAMES, $RIGHT_FRAMES))")

ffmpeg -y -i "$LEFT" -i "$RIGHT" \
  -filter_complex "
    [0:v]loop=loop=${LOOPS_FOR_LEFT}:size=${LEFT_FRAMES}:start=0,scale=-2:${MAX_H}:flags=lanczos,format=rgba,pad=iw+${GAP}:ih:0:0:color=white[left];
    [1:v]loop=loop=${LOOPS_FOR_RIGHT}:size=${RIGHT_FRAMES}:start=0,scale=-2:${MAX_H}:flags=lanczos,format=rgba[right];
    [left][right]xstack=inputs=2:layout=0_0|w0_0:shortest=1,trim=duration=${LEFT_DUR},split[s0][s1];
    [s0]palettegen=max_colors=256:stats_mode=full[pal];
    [s1][pal]paletteuse=dither=sierra2_4a
  " "$OUT"

echo "Created $OUT"
