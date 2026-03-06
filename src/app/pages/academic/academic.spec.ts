import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Academic } from './academic';

describe('Academic', () => {
  let component: Academic;
  let fixture: ComponentFixture<Academic>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Academic]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Academic);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
