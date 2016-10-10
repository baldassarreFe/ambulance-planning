(define (problem test)
(:domain ambulance world)
(:objects l0 l1 l2 l3 l4 l5 l6 l7 l8 l9 p0 p1 p2 p3 p4 a0 a1 a2 h0 h1 )
(:init (Location(l0,3,4,30))
(Location(l1,5,0,11))
(Road(l0,l1,4.705608988449683))
(Location(l2,7,0,15))
(Road(l1,l2,2.9241114982259724))
(Location(l3,9,8,47))
(Road(l1,l3,9.618926429485414))
(Location(l4,0,6,26))
(Road(l2,l4,9.338865522080114))
(Location(l5,4,3,15))
(Road(l4,l5,5.429418201513057))
(Location(l6,7,1,11))
(Road(l0,l6,5.792403204272906))
(Location(l7,3,2,2))
(Road(l4,l7,5.062804083725759))
(Location(l8,1,8,28))
(Road(l5,l8,6.254805149242165))
(Location(l9,5,3,35))
(Road(l3,l9,6.615279025747052))
(Patient(p0,1,59))
(Patient(p1,3,0))
(Patient(p2,3,0))
(Patient(p3,3,87))
(Patient(p4,1,0))
(Ambulance(a0))
(Ambulance(a1))
(Ambulance(a2))
(Hospital(h0))
(Hospital(h1))
(At(p0,9,8))
(At(p1,5,3))
(At(p2,3,4))
(At(p3,1,8))
(At(p4,0,6))
(Waiting(p0))
(Waiting(p1))
(Waiting(p2))
(Waiting(p3))
(Waiting(p4))
(At(a0,4,3))
(Available(a0))
(At(a1,7,0))
(Available(a1))
(At(a2,7,0))
(Available(a2))
(At(h0,4,3))
(At(h1,7,1))
)
(:goal (InHospital(p0))
(InHospital(p1))
(InHospital(p2))
(InHospital(p3))
(InHospital(p4))
))