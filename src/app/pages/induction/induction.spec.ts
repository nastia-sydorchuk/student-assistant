import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Induction } from './induction';

describe('Induction', () => {
  let component: Induction;
  let fixture: ComponentFixture<Induction>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Induction]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Induction);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
