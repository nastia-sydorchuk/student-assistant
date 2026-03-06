import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PostgradInduction } from './postgrad-induction';

describe('PostgradInduction', () => {
  let component: PostgradInduction;
  let fixture: ComponentFixture<PostgradInduction>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostgradInduction]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PostgradInduction);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
