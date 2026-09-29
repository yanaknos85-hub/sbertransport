import React, { useState, useRef } from 'react';

import {
  ButtonArrowLeft, ButtonArrowRight, CarouselElevent, CarouselItems
} from './styledCarousel';
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import CarouselItem from './carouselItem/CarouselItem';
import { TripRequestModel } from 'stores/Trip/models';

interface CarouselProps {
  items: TripRequestModel[];
  direction?: 'column' | 'row';
}

const Carousel: React.FC<CarouselProps> = ({ items, direction = 'row' }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStartX, setDragStartX] = useState(0);
  const carouselRef = useRef(null);

  const handleMouseDown = event => {
    setIsDragging(true);
    setDragStartX(event.clientX);
  };

  const handleMouseMove = event => {
    if (!isDragging) return;

    const delta = event.clientX - dragStartX;
    carouselRef.current.style.transform = `translateX(calc(-${currentIndex * 25}% + ${delta}px))`;
  };

  const handleMouseUp = event => {
    setIsDragging(false);
    const delta = dragStartX - event.clientX;

    if (Math.abs(delta) > 50) {
      setCurrentIndex(prevIndex => {
        return delta > 0 && currentIndex !== items.length - 4
          ? Math.min(prevIndex + 1, items.length - 1)
          : Math.max(prevIndex - 1, 0);
      });
    }
    carouselRef.current.style.transition = 'transform 0.5s ease';
    carouselRef.current.style.transform = `translateX(-${currentIndex * 25}%)`;
  };

  const handleMouseLeave = event => {
    if (isDragging) {
      handleMouseUp(event);
    }
  };

  const goToNextSlide = () => {
    setCurrentIndex(prevIndex => (prevIndex === items.length - 1 ? prevIndex : prevIndex + 1));
  };

  const goToPrevSlide = () => {
    setCurrentIndex(prevIndex => (prevIndex === 0 ? prevIndex : prevIndex - 1));
  };

  return (
    <CarouselElevent
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseLeave}
    >
      {currentIndex !== 0 && (
        <ButtonArrowLeft onClick={goToPrevSlide}>
          <LeftOutlined />
        </ButtonArrowLeft>
      )}
      <CarouselItems
        ref={carouselRef}
        style={{ transform: `translateX(-${currentIndex * 25}%)` }}
        direction={direction}
      >
        {items?.map((item, index) => (
          <CarouselItem item={item} key={index} />
        ))}
      </CarouselItems>
      {currentIndex !== items.length - 4 && items.length > 4 && (
        <ButtonArrowRight onClick={goToNextSlide}>
          <RightOutlined />
        </ButtonArrowRight>
      )}
    </CarouselElevent>
  );
};

export default Carousel;
