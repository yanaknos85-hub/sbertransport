/* eslint-disable @typescript-eslint/no-explicit-any */
import React, {
  FC, useCallback, useEffect, useRef
} from 'react';
import { Slider } from 'antd';
import { SliderRangeProps } from 'antd/lib/slider';

type ColoredSliderProps = Omit<SliderRangeProps, 'range'> & { colors?: [string, string, string] };

const ColoredSlider: FC<ColoredSliderProps> = ({ colors, ...other }) => {
  const sliderRef = useRef();
  const trackRefs = useRef<{ left?: HTMLDivElement; middle?: HTMLDivElement; right?: HTMLDivElement }>({});
  const handleRefs = useRef<{ left?: HTMLDivElement; right?: HTMLDivElement }>({});

  useEffect(() => {
    setTimeout(() => {
      if (sliderRef) {
        const sliderNode: HTMLDivElement = (sliderRef.current as any).sliderRef;

        const tracks = trackRefs.current;

        tracks.middle = sliderNode.getElementsByClassName('ant-slider-track-1')[0] as HTMLDivElement;
        if (tracks.middle) {
          const defaultTrackIndex = Array.from(sliderNode.childNodes).indexOf(tracks.middle);

          tracks.left = document.createElement('div');
          tracks.left.className = 'ant-slider-track ant-slider-track-before';

          tracks.right = document.createElement('div');
          tracks.right.className = 'ant-slider-track ant-slider-track-after';

          sliderNode.insertBefore(tracks.right, sliderNode.childNodes[defaultTrackIndex + 1]);
          sliderNode.insertBefore(tracks.left, sliderNode.childNodes[defaultTrackIndex]);
        }

        handleRefs.current.left = sliderNode.getElementsByClassName('ant-slider-handle-1')[0] as HTMLDivElement;
        handleRefs.current.right = sliderNode.getElementsByClassName('ant-slider-handle-2')[0] as HTMLDivElement;
      }
    });
  }, [sliderRef]);

  useEffect(() => {
    const {
      left, middle, right,
    } = trackRefs.current;
    if (left && right && middle) {
      [left.style.backgroundColor, middle.style.backgroundColor, right.style.backgroundColor] = colors!;
    }
    const { left: leftHandle, right: rightHandle } = handleRefs.current;
    if (leftHandle && rightHandle) {
      const [color1, color2, color3] = colors!;
      leftHandle.style.borderLeftColor = color1;
      leftHandle.style.borderBottomColor = color1;
      leftHandle.style.borderRightColor = color2;
      leftHandle.style.borderTopColor = color2;

      rightHandle.style.borderTopColor = color2;
      rightHandle.style.borderLeftColor = color2;
      rightHandle.style.borderRightColor = color3;
      rightHandle.style.borderBottomColor = color3;
    }
  }, [colors]);

  const syncTracks = useCallback(() => {
    const {
      left, middle, right,
    } = trackRefs.current;
    if (left && right && middle) {
      left.style.width = middle.style.left;
      right.style.left = `calc(${middle.style.left} + ${middle.style.width})`;
      right.style.width = `calc(100% - ${middle.style.left} - ${middle.style.width})`;
    }
  }, []);

  useEffect(() => {
    if (other.value) {
      syncTracks();
    }
  }, [other.value, syncTracks]);

  const onChange = (value: [number, number]) => {
    if (typeof other.onChange === 'function') {
      other.onChange(value);
    }
  };

  return (
    <Slider
      {...other}
      onChange={onChange}
      ref={sliderRef}
      range
    />
  );
};

ColoredSlider.defaultProps = {
  colors: ['#ff0000', '#dddd17', '#00ff00'],
};

export default ColoredSlider;
