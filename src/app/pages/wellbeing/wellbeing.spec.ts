import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Wellbeing } from './wellbeing';

describe('Wellbeing', () => {
  let component: Wellbeing;
  let fixture: ComponentFixture<Wellbeing>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Wellbeing]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Wellbeing);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
